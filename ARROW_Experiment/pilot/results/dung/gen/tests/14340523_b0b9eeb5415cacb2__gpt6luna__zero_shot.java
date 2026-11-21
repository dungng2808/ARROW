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
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        healthData.setPatient(patient);
        patient.setDoctors(Collections.<Doctor>emptyList());

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.<HealthDataValue>emptyList());

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void generateWithNoSettingsReturnsEmptyList() {
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        healthData.setPatient(patient);
        patient.setDoctors(Collections.singletonList(doctor));
        doctor.setSettings(Collections.<DoctorNotificationSetting>emptyList());

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.<HealthDataValue>emptyList());

        assertEquals(0, result.size());
    }

    @Test
    public void generateCreatesNotificationsForEnabledSettingsAndEveryValue() {
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        DoctorNotificationSetting enabled = new DoctorNotificationSetting();
        enabled.setEnabled(Boolean.TRUE);
        enabled.setProviderId("provider");
        HealthDataValue first = new HealthDataValue();
        HealthDataValue second = new HealthDataValue();

        healthData.setPatient(patient);
        patient.setDoctors(Collections.singletonList(doctor));
        doctor.setSettings(Collections.singletonList(enabled));

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Arrays.asList(first, second));

        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
    }

    @Test
    public void generateSkipsDisabledAndUnspecifiedSettings() {
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        DoctorNotificationSetting disabled = new DoctorNotificationSetting();
        disabled.setEnabled(Boolean.FALSE);
        DoctorNotificationSetting unspecified = new DoctorNotificationSetting();

        healthData.setPatient(patient);
        patient.setDoctors(Collections.singletonList(doctor));
        doctor.setSettings(Arrays.asList(disabled, unspecified));

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.singletonList(new HealthDataValue()));

        assertEquals(0, result.size());
    }
}
