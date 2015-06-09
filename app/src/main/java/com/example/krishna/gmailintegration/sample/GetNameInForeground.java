package com.example.krishna.gmailintegration.sample;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class GetNameInForeground extends AbstractGetNameTask {

  public GetNameInForeground(SplashActivity activity, String email, String scope) {
      super(activity, email, scope);
  }


  /**
   * Get a authentication token if one is not available. If the error is not recoverable then
   * it displays the error message on parent activity right away.
   */
  @Override
  protected String fetchToken() throws IOException {
      try {
          return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
      } catch (GooglePlayServicesAvailabilityException playEx) {
          // GooglePlayServices.apk is either old, disabled, or not present.
      } catch (UserRecoverableAuthException userRecoverableException) {
          // Unable to authenticate, but the user can fix this.
          // Forward the user to the appropriate activity.
          mActivity.startActivityForResult(userRecoverableException.getIntent(), mRequestCode);
      } catch (GoogleAuthException fatalException) {
          onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
      }
      return null;
  }
}