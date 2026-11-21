package info.ozkan.vipera.business.notification;

import info.ozkan.vipera.entities.Doctor;
import info.ozkan.vipera.entities.DoctorNotificationSetting;
import info.ozkan.vipera.entities.HealthData;
import info.ozkan.vipera.entities.HealthDataValue;
import info.ozkan.vipera.entities.Notification;
import info.ozkan.vipera.entities.Patient;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class NotificationGenerator_ArrowTest {

    @Test
    public void testGenerate_EmptyFilteredValues() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = new HealthData();
        final Patient patient = new Patient();
        final Doctor doctor = new Doctor();
        final List<DoctorNotificationSetting> settings = new ArrayList<DoctorNotificationSetting>();
        final DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(Boolean.TRUE);
        setting.setProviderId("provider1");
        settings.add(setting);
        doctor.setSettings(settings);
        final List<Doctor> doctors = new ArrayList<Doctor>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGenerate_NoDoctors() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = new HealthData();
        final Patient patient = new Patient();
        final List<Doctor> doctors = new ArrayList<Doctor>();
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        filteredValues.add(new HealthDataValue());
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGenerate_NoSettings() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = new HealthData();
        final Patient patient = new Patient();
        final Doctor doctor = new Doctor();
        final List<Doctor> doctors = new ArrayList<Doctor>();
        doctors.add(doctor);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        filteredValues.add(new HealthDataValue());
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGenerate_DisabledSetting() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = new HealthData();
        final Patient patient = new Patient();
        final Doctor doctor = new Doctor();
        final List<DoctorNotificationSetting> settings = new ArrayList<DoctorNotificationSetting>();
        final DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(Boolean.FALSE);
        setting.setProviderId("provider1");
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
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGenerate_NullSetting() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = new HealthData();
        final Patient patient = new Patient();
        final Doctor doctor = new Doctor();
        final List<DoctorNotificationSetting> settings = new ArrayList<DoctorNotificationSetting>();
        final DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(null);
        setting.setProviderId("provider1");
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
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGenerate_SingleDoctorSingleValue() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = new HealthData();
        final Patient patient = new Patient();
        final Doctor doctor = new Doctor();
        final List<DoctorNotificationSetting> settings = new ArrayList<DoctorNotificationSetting>();
        final DoctorNotificationSetting setting = new DoctorNotificationSetting();
        setting.setEnabled(Boolean.TRUE);
        setting.setProviderId("provider1");
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
        assertEquals(1, result.size());
        assertEquals(patient, result.get(0).getPatient());
        assertEquals(doctor, result.get(0).getDoctor());
        assertEquals("provider1", result.get(0).getProviderId());
        assertEquals(filteredValues.get(0), result.get(0).getValue());
    }
    
    @Test
    public void testGenerate_MultipleDoctorsMultipleValues() {
        final NotificationGenerator generator = new NotificationGenerator();
        final HealthData healthData = new HealthData();
        final Patient patient = new Patient();
        final Doctor doctor1 = new Doctor();
        final Doctor doctor2 = new Doctor();
        final List<DoctorNotificationSetting> settings1 = new ArrayList<DoctorNotificationSetting>();
        final DoctorNotificationSetting setting1 = new DoctorNotificationSetting();
        setting1.setEnabled(Boolean.TRUE);
        setting1.setProviderId("provider1");
        settings1.add(setting1);
        doctor1.setSettings(settings1);
        final List<DoctorNotificationSetting> settings2 = new ArrayList<DoctorNotificationSetting>();
        final DoctorNotificationSetting setting2 = new DoctorNotificationSetting();
        setting2.setEnabled(Boolean.TRUE);
        setting2.setProviderId("provider2");
        settings2.add(setting2);
        doctor2.setSettings(settings2);
        final List<Doctor> doctors = new ArrayList<Doctor>();
        doctors.add(doctor1);
        doctors.add(doctor2);
        patient.setDoctors(doctors);
        healthData.setPatient(patient);
        final List<HealthDataValue> filteredValues = new ArrayList<HealthDataValue>();
        filteredValues.add(new HealthDataValue());
        filteredValues.add(new HealthDataValue());
        
        final List<Notification> result = generator.generate(healthData, filteredValues);
        
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(patient, result.get(0).getPatient());
        assertEquals(doctor1, result.get(0).getDoctor());
        assertEquals("provider1", result.get(0).getProviderId());
        assertEquals(filteredValues.get(0), result.get(0).getValue());
        assertEquals(patient, result.get(1).getPatient());
        assertEquals(doctor1, result.get(1).getDoctor());
        assertEquals("provider1", result.get(1).getProviderId());
        assertEquals(filteredValues.get(1), result.get(1).getValue());
        assertEquals(patient, result.get(2).getPatient());
        assertEquals(doctor2, result.get(2).getDoctor());
        assertEquals("provider2", result.get(2).getProviderId());
        assertEquals(filteredValues.get(0), result.get(2).getValue());
        assertEquals(patient, result.get(3).getPatient());
        assertEquals(doctor2, result.get(3).getDoctor());
        assertEquals("provider2", result.get(3).getProviderId());
        assertEquals(filteredValues.get(1), result.get(3).getValue());
    }
}
