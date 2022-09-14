package com.gauravbora.phoneauthentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {
    private  lateinit var sendOTPButton: Button
    private lateinit var phoneNumber: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var number: String
    private lateinit var phoneProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        sendOTPButton=findViewById(R.id.sendOTPButton)
      phoneNumber=findViewById(R.id.phoneNumber)
       auth=FirebaseAuth.getInstance()
        phoneProgressBar=findViewById(R.id.phoneProgressBar)
        phoneProgressBar.visibility= View.INVISIBLE

        sendOTPButton.setOnClickListener{
            number=phoneNumber.text.trim().toString()
            if(number.isNotEmpty()){
                if(number.length==10){

                    number="+91$number"
                    phoneProgressBar.visibility= View.VISIBLE

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)


                }else{
                    Toast.makeText(this,"Please Enter Correct Number",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Please Enter a Number",Toast.LENGTH_SHORT).show()
            }

        }


    }
   val  callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.e("TAG","onVarificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.e("TAG","onVarificationFailed: ${e.toString()}")
            }

            phoneProgressBar.visibility=View.VISIBLE
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.


            // Save verification ID and resending token so we can use them later
          val intent= Intent(this@PhoneActivity,OTPActivity::class.java)
            intent.putExtra("OTP",verificationId)
            intent.putExtra("resendToken",token)
            intent.putExtra("phoneNumber",number)
            startActivity(intent)
            phoneProgressBar.visibility=View.INVISIBLE
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this,"Authenticate Successfully",Toast.LENGTH_SHORT).show()
                    sendToMain()

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG","signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
                phoneProgressBar.visibility=View.VISIBLE
            }
    }
    private fun sendToMain(){
        startActivity(Intent(this , MainActivity::class.java))
    }


    override fun onStart() {
        super.onStart()
        if(auth.currentUser!=null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }

}