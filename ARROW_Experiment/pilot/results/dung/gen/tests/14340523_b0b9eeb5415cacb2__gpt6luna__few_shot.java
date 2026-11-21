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
import static org.junit.Assert.assertTrue;

public class NotificationGenerator_ArrowTest {

    @Test
    public void generateReturnsEmptyListWhenPatientHasNoDoctors() {
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        patient.setDoctors(Collections.<Doctor>emptyList());
        healthData.setPatient(patient);

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.<HealthDataValue>emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    public void generateReturnsEmptyListWhenDoctorHasNoEnabledSettings() {
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(false);
        doctor.setSettings(Collections.singletonList(setting));
        patient.setDoctors(Collections.singletonList(doctor));
        healthData.setPatient(patient);

        List<Notification> result =
                new NotificationGenerator().generate(healthData, Collections.<HealthDataValue>emptyList());

        assertTrue(result.isEmpty());
    }
}
