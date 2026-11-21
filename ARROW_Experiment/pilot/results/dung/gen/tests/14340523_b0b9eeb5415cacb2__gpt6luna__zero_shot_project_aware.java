package info.ozkan.vipera.business.notification;

import info.ozkan.vipera.entities.Doctor;
import info.ozkan.vipera.entities.DoctorNotificationSetting;
import info.ozkan.vipera.entities.HealthData;
import info.ozkan.vipera.entities.HealthDataValue;
import info.ozkan.vipera.entities.Notification;
import info.ozkan.vipera.entities.Patient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NotificationGenerator_ArrowTest {

    @Test
    public void generateWithNoDoctorsReturnsEmptyList() {
        Patient patient = new Patient();
        patient.setDoctors(Collections.<Doctor>emptyList());
        HealthData healthData = new HealthData();
        healthData.setPatient(patient);

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.<HealthDataValue>emptyList());

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void generateWithDoctorAndNoEnabledSettingsReturnsEmptyList() {
        Doctor doctor = new Doctor();
        doctor.setSettings(Collections.<DoctorNotificationSetting>emptyList());
        Patient patient = new Patient();
        patient.setDoctors(Collections.singletonList(doctor));
        HealthData healthData = new HealthData();
        healthData.setPatient(patient);

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.<HealthDataValue>emptyList());

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void generateCreatesNotificationForEachValueWhenSettingEnabled() {
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(Boolean.TRUE);
        setting.setProviderId("provider");
        Doctor doctor = new Doctor();
        doctor.setSettings(Collections.singletonList(setting));
        Patient patient = new Patient();
        patient.setDoctors(Collections.singletonList(doctor));
        HealthData healthData = new HealthData();
        healthData.setPatient(patient);

        HealthDataValue first = new HealthDataValue();
        HealthDataValue second = new HealthDataValue();
        List<HealthDataValue> values = Arrays.asList(first, second);

        List<Notification> result = new NotificationGenerator().generate(healthData, values);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void generateCreatesNoNotificationsForDisabledSetting() {
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(Boolean.FALSE);
        Doctor doctor = new Doctor();
        doctor.setSettings(Collections.singletonList(setting));
        Patient patient = new Patient();
        patient.setDoctors(Collections.singletonList(doctor));
        HealthData healthData = new HealthData();
        healthData.setPatient(patient);

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.<HealthDataValue>emptyList());

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
