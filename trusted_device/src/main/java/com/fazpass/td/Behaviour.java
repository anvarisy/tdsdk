package com.fazpass.td;

import androidx.annotation.NonNull;

public interface Behaviour {
   void enrollDeviceByPin(User user, @NonNull String pin, TrustedDeviceListener<EnrollStatus> enroll);

   void enrollDeviceByFinger(User user, String pin, TrustedDeviceListener<EnrollStatus> enroll);

   void validateUser(String pin, TrustedDeviceListener<ValidateStatus> listener);

   void removeDevice(TrustedDeviceListener<RemoveStatus> listener);
}
