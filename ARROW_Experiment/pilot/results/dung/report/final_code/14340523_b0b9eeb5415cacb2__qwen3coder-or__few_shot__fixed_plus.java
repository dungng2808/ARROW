package info.ozkan.vipera.business.notification;

import info.ozkan.vipera.entities.Doctor;
import info.ozkan.vipera.entities.DoctorNotificationSetting;
import info.ozkan.vipera.entities.HealthData;
import info.ozkan.vipera.entities.HealthDataValue;
import info.ozkan.vipera.entities.Notification;
import info.ozkan.vipera.entities.Patient;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NotificationGenerator_ArrowTest {

    @Test
    public void testGenerate_EmptyFilteredValues() {
        NotificationGenerator generator = new NotificationGenerator();
        
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        List<DoctorNotificationSetting> settings = new ArrayList<>();
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(true);
        setting.setProviderId("provider1");
        settings.add(setting);
        doctor.setSettings(settings);
        List<Doctor> doctors = new ArrayList<>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        
        List<Notification> result = generator.generate(healthData, filteredValues);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGenerate_NoDoctors() {
        NotificationGenerator generator = new NotificationGenerator();
        
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        patient.setDoctors(new ArrayList<>());
        healthData.setPatient(patient);
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value = new HealthDataValue();
        filteredValues.add(value);
        
        List<Notification> result = generator.generate(healthData, filteredValues);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGenerate_NoSettings() {
        NotificationGenerator generator = new NotificationGenerator();
        
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        doctor.setSettings(new ArrayList<>());
        List<Doctor> doctors = new ArrayList<>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value = new HealthDataValue();
        filteredValues.add(value);
        
        List<Notification> result = generator.generate(healthData, filteredValues);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGenerate_DisabledSetting() {
        NotificationGenerator generator = new NotificationGenerator();
        
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        List<DoctorNotificationSetting> settings = new ArrayList<>();
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(false);
        setting.setProviderId("provider1");
        settings.add(setting);
        doctor.setSettings(settings);
        List<Doctor> doctors = new ArrayList<>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value = new HealthDataValue();
        filteredValues.add(value);
        
        List<Notification> result = generator.generate(healthData, filteredValues);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGenerate_NullSetting() {
        NotificationGenerator generator = new NotificationGenerator();
        
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        List<DoctorNotificationSetting> settings = new ArrayList<>();
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(null);
        setting.setProviderId("provider1");
        settings.add(setting);
        doctor.setSettings(settings);
        List<Doctor> doctors = new ArrayList<>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value = new HealthDataValue();
        filteredValues.add(value);
        
        List<Notification> result = generator.generate(healthData, filteredValues);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGenerate_ValidCase() {
        NotificationGenerator generator = new NotificationGenerator();
        
        HealthData healthData = new HealthData();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        List<DoctorNotificationSetting> settings = new ArrayList<>();
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(true);
        setting.setProviderId("provider1");
        settings.add(setting);
        doctor.setSettings(settings);
        List<Doctor> doctors = new ArrayList<>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value1 = new HealthDataValue();
        HealthDataValue value2 = new HealthDataValue();
        filteredValues.add(value1);
        filteredValues.add(value2);
        
        List<Notification> result = generator.generate(healthData, filteredValues);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(patient, result.get(0).getPatient());
        assertEquals(doctor, result.get(0).getDoctor());
        assertEquals("provider1", result.get(0).getProviderId());
        assertEquals(value1, result.get(0).getValue());
        assertEquals(patient, result.get(1).getPatient());
        assertEquals(doctor, result.get(1).getDoctor());
        assertEquals("provider1", result.get(1).getProviderId());
        assertEquals(value2, result.get(1).getValue());
    }

    @Test
    public void testCreateNotifications_EmptyFilteredValues() {
        NotificationGenerator generator = new NotificationGenerator();
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        List<DoctorNotificationSetting> settings = new ArrayList<>();
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(true);
        setting.setProviderId("provider1");
        settings.add(setting);
        doctor.setSettings(settings);
        
        List<Notification> result = generator.createNotifications(filteredValues, patient, doctor);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCreateNotifications_NoSettings() {
        NotificationGenerator generator = new NotificationGenerator();
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value = new HealthDataValue();
        filteredValues.add(value);
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        doctor.setSettings(new ArrayList<>());
        
        List<Notification> result = generator.createNotifications(filteredValues, patient, doctor);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCreateNotifications_DisabledSetting() {
        NotificationGenerator generator = new NotificationGenerator();
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value = new HealthDataValue();
        filteredValues.add(value);
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        List<DoctorNotificationSetting> settings = new ArrayList<>();
        DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(false);
        setting.setProviderId("provider1");
        settings.add(setting);
        doctor.setSettings(settings);
        
        List<Notification> result = generator.createNotifications(filteredValues, patient, doctor);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCreateNotifications_MultipleValuesMultipleSettings() {
        NotificationGenerator generator = new NotificationGenerator();
        
        List<HealthDataValue> filteredValues = new ArrayList<>();
        HealthDataValue value1 = new HealthDataValue();
        HealthDataValue value2 = new HealthDataValue();
        filteredValues.add(value1);
        filteredValues.add(value2);
        
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        List<DoctorNotificationSetting> settings = new ArrayList<>();
        DoctorNotificationSetting setting1 = new DoctorNotificationSetting();
        setting1.setEnabled(true);
        setting1.setProviderId("provider1");
        DoctorNotificationSetting setting2 = new DoctorNotificationSetting();
        setting2.setEnabled(true);
        setting2.setProviderId("provider2");
        settings.add(setting1);
        settings.add(setting2);
        doctor.setSettings(settings);
        
        List<Notification> result = generator.createNotifications(filteredValues, patient, doctor);
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(patient, result.get(0).getPatient());
        assertEquals(doctor, result.get(0).getDoctor());
        assertEquals("provider1", result.get(0).getProviderId());
        assertEquals(value1, result.get(0).getValue());
        assertEquals(patient, result.get(1).getPatient());
        assertEquals(doctor, result.get(1).getDoctor());
        assertEquals("provider1", result.get(1).getProviderId());
        assertEquals(value2, result.get(1).getValue());
        assertEquals(patient, result.get(2).getPatient());
        assertEquals(doctor, result.get(2).getDoctor());
        assertEquals("provider2", result.get(2).getProviderId());
        assertEquals(value1, result.get(2).getValue());
        assertEquals(patient, result.get(3).getPatient());
        assertEquals(doctor, result.get(3).getDoctor());
        assertEquals("provider2", result.get(3).getProviderId());
        assertEquals(value2, result.get(3).getValue());
    }
}
