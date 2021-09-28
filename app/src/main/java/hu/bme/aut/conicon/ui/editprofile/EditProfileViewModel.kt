package hu.bme.aut.conicon.ui.editprofile

import android.net.Uri
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.ConversationElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class EditProfileViewModel @Inject constructor(

) : RainbowCakeViewModel<EditProfileViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun updateProfilePicture(uri: Uri) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        val currentDate = Date().time

        // The photo will be saved in the user's profile_pictures directory
        // Its name will be the current date in long
        val storageReference =
                FirebaseStorage.getInstance().reference.child("users/$uid/profile_pictures/$currentDate")

        storageReference.putFile(uri).addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { url ->
                val userCollection = FirebaseFirestore.getInstance().collection("users")
                userCollection.document(uid).update(
                        "photoUrl", url.toString()
                ).addOnSuccessListener {
                    viewState = UploadReady
                }.addOnFailureListener { ex ->
                    viewState = FirebaseError(ex.message.toString())
                }
            }.addOnFailureListener { ex ->
                viewState = FirebaseError(ex.message.toString())
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }

    fun updateUsername(username: String) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        val query = userCollection.whereEqualTo("username", username)
        query.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                // If username is not taken
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid.toString()

                userCollection.document(uid).update(
                        "username", username
                ).addOnSuccessListener {
                    viewState = SuccessfullyUpdated
                }.addOnFailureListener { ex ->
                    viewState = FirebaseError(ex.message.toString())
                }
            } else {
                // If username is taken
                viewState = UsernameTakenError
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }

    fun deleteProfile(user: AppUser) = viewModelScope.launch {
        viewState = Loading
        delay(500)

        val postCollection = FirebaseFirestore.getInstance().collection("posts")
        if (user.posts.isNotEmpty()) {
            for (postID in user.posts) {
                postCollection.document(postID).delete()
            }
        }

        val conversationCollection = FirebaseFirestore.getInstance().collection("conversations")
        val query = conversationCollection.whereEqualTo("participantIDs.${user.id}", true)
        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    val conversation = document.toObject(ConversationElement::class.java)
                    conversationCollection.document(conversation!!.id).delete()
                }
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }

        val userCollection = FirebaseFirestore.getInstance().collection("users")
        userCollection.document(user.id).delete().addOnSuccessListener {
            FirebaseAuth.getInstance().currentUser?.delete()?.addOnSuccessListener {
                FirebaseAuth.getInstance().signOut()
                viewState = ProfileDeleted
            }?.addOnFailureListener { ex ->
                viewState = FirebaseError(ex.message.toString())
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }
    }
}