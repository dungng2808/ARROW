package info.ozkan.vipera.business.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import info.ozkan.vipera.entities.Doctor;
import info.ozkan.vipera.entities.DoctorNotificationSetting;
import info.ozkan.vipera.entities.HealthData;
import info.ozkan.vipera.entities.HealthDataValue;
import info.ozkan.vipera.entities.Notification;
import info.ozkan.vipera.entities.Patient;

public class NotificationGenerator_ArrowTest {

    @Test
    public void testGenerate_EmptyFilteredValues_ReturnsEmptyList() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = createHealthData();
        final Patient patient = createPatient();
        final Doctor doctor = createDoctor();
        final List<Doctor> doctors = new ArrayList<Doctor>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testGenerate_NoDoctors_ReturnsEmptyList() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = createHealthData();
        final Patient patient = createPatient();
        patient.setDoctors(new ArrayList<Doctor>());
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        filteredValues.add(new HealthDataValue());
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testGenerate_NoSettings_ReturnsEmptyList() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = createHealthData();
        final Patient patient = createPatient();
        final Doctor doctor = createDoctor();
        doctor.setSettings(null);
        final List<Doctor> doctors = new ArrayList<Doctor>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        filteredValues.add(new HealthDataValue());
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testGenerate_DisabledSettings_ReturnsEmptyList() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = createHealthData();
        final Patient patient = createPatient();
        final Doctor doctor = createDoctor();
        final DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(false);
        final List<DoctorNotificationSetting> settings = new ArrayList<DoctorNotificationSetting>();
        settings.add(setting);
        doctor.setSettings(settings);
        final List<Doctor> doctors = new ArrayList<Doctor>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        filteredValues.add(new HealthDataValue());
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testGenerate_EnabledSettingsWithValues_ReturnsNotifications() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = createHealthData();
        final Patient patient = createPatient();
        patient.setId(1L);
        patient.setTckn(12345678901L);
        final Doctor doctor = createDoctor();
        doctor.setId(1L);
        doctor.setSettings(new ArrayList<DoctorNotificationSetting>());
        final DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(true);
        setting.setProviderId("provider1");
        doctor.getSettings().add(setting);
        final List<Doctor> doctors = new ArrayList<Doctor>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        filteredValues.add(new HealthDataValue());
        filteredValues.add(new HealthDataValue());
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(patient, result.get(0).getPatient());
        assertEquals(doctor, result.get(0).getDoctor());
        assertEquals("provider1", result.get(0).getProvider());
        assertEquals(patient, result.get(1).getPatient());
        assertEquals(doctor, result.get(1).getDoctor());
        assertEquals("provider1", result.get(1).getProvider());
    }
    
    private HealthData createHealthData() {
        return new HealthData();
    }
    
    private Patient createPatient() {
        return new Patient();
    }
    
    private Doctor createDoctor() {
        return new Doctor();
    }
}
