# dual_auth_firebase_demo
You can allow users to sign in to your app using email ?&amp; phone both by linking auth provider credentials with same token Id.

### Before you begin
* Create your firebase account
* Add your json file into your app
* In firebase Authentication enable for two authentication providers to your app.
  * Email Auth Provider
  * Phone Auth provider
* Choose realtime database to store auth data
* Add all needed dependencies into `build.gradle`

#### To link auth provider credentials to an existing user account:
* Sign in the user using email method.
* Complete the sign-in flow for the new authentication provider up to `FirebaseAuth.signInWith` methods.
* Pass the `Credential` object to the signed-in user's` linkWithCredential` method:


         mAuth.getCurrentUser().linkWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "linkWithCredential:success");
                    FirebaseUser user = task.getResult().getUser();
                    updateUI(user);
                } else {
                    Log.w(TAG, "linkWithCredential:failure", task.getException());
                    Toast.makeText(AnonymousAuthActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
  
        
