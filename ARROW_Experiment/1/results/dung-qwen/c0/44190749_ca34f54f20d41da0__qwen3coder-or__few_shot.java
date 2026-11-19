package org.asqatasun.webapp.controller;

import org.asqatasun.entity.contract.Contract;
import org.asqatasun.entity.functionality.Functionality;
import org.asqatasun.entity.service.contract.ContractDataService;
import org.asqatasun.webapp.dto.factory.DetailedContractInfoFactory;
import org.asqatasun.webapp.exception.ForbiddenPageException;
import org.asqatasun.webapp.exception.ForbiddenUserException;
import org.asqatasun.webapp.util.TgolKeyStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContractController_ArrowTest {

    @Mock
    private ContractDataService contractDataService;

    @Mock
    private DetailedContractInfoFactory detailedContractInfoFactory;

    @Mock
    private LocaleResolver localeResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    private ContractController contractController;

    @Before
    public void setUp() {
        contractController = new ContractController(contractDataService, detailedContractInfoFactory, localeResolver);
    }

    @Test
    public void testDisplayContractPage_InvalidContractId() {
        try {
            contractController.displayContractPage("invalid", request, model);
            fail("Expected ForbiddenPageException to be thrown");
        } catch (ForbiddenPageException e) {
            // Expected
        }
    }

    @Test
    public void testDisplayContractPage_UnauthorizedContract() {
        when(contractDataService.getAllContractsByUser(any())).thenReturn(new ArrayList<>());
        
        try {
            contractController.displayContractPage("1", request, model);
            fail("Expected ForbiddenUserException to be thrown");
        } catch (ForbiddenUserException e) {
            // Expected
        }
    }

    @Test
    public void testDisplayContractPage_ValidContract_NotExpired() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setFunctionalitySet(new HashSet<>());
        
        List<Contract> contracts = Arrays.asList(contract);
        when(contractDataService.getAllContractsByUser(any())).thenReturn(contracts);
        when(contractDataService.read(1L)).thenReturn(contract);
        when(localeResolver.resolveLocale(request)).thenReturn(Locale.ENGLISH);
        when(detailedContractInfoFactory.getDetailedContractInfo(contract)).thenReturn(new Object());
        
        String result = contractController.displayContractPage("1", request, model);
        
        assertEquals(TgolKeyStore.CONTRACT_VIEW_NAME, result);
        verify(model).addAttribute(TgolKeyStore.LOCALE_KEY, Locale.ENGLISH);
        verify(model).addAttribute(TgolKeyStore.CONTRACT_ID_VALUE, 1L);
        verify(model).addAttribute(TgolKeyStore.DETAILED_CONTRACT_INFO, any());
        verify(model).addAttribute(TgolKeyStore.IS_CONTRACT_EXPIRED_KEY, false);
    }

    @Test
    public void testDisplayContractPage_ValidContract_Expired() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setFunctionalitySet(new HashSet<>());
        contract.setExpiryDate(new Date(System.currentTimeMillis() - 1000)); // Past date
        
        List<Contract> contracts = Arrays.asList(contract);
        when(contractDataService.getAllContractsByUser(any())).thenReturn(contracts);
        when(contractDataService.read(1L)).thenReturn(contract);
        
        try {
            contractController.displayContractPage("1", request, model);
            fail("Expected ForbiddenUserException to be thrown");
        } catch (ForbiddenUserException e) {
            // Expected
        }
    }

    @Test
    public void testIsContractHasFunctionalityAllowingTrend_True() {
        Contract contract = new Contract();
        Set<Functionality> functionalitySet = new HashSet<>();
        Functionality functionality = new Functionality();
        functionality.setCode("TREND_FUNCTIONALITY");
        contract.setFunctionalitySet(functionalitySet);
        
        List<String> authorizedFunctionalityForTrend = Arrays.asList("TREND_FUNCTIONALITY");
        
        // Mock the field using reflection since it's private
        try {
            java.lang.reflect.Field field = ContractController.class.getDeclaredField("authorizedFunctionalityForTrend");
            field.setAccessible(true);
            field.set(contractController, authorizedFunctionalityForTrend);
        } catch (Exception e) {
            fail("Failed to set authorizedFunctionalityForTrend field");
        }
        
        boolean result = contractController.isContractHasFunctionalityAllowingTrend(contract);
        assertTrue(result);
    }

    @Test
    public void testIsContractHasFunctionalityAllowingTrend_False() {
        Contract contract = new Contract();
        Set<Functionality> functionalitySet = new HashSet<>();
        Functionality functionality = new Functionality();
        functionality.setCode("OTHER_FUNCTIONALITY");
        contract.setFunctionalitySet(functionalitySet);
        
        List<String> authorizedFunctionalityForTrend = Arrays.asList("TREND_FUNCTIONALITY");
        
        // Mock the field using reflection since it's private
        try {
            java.lang.reflect.Field field = ContractController.class.getDeclaredField("authorizedFunctionalityForTrend");
            field.setAccessible(true);
            field.set(contractController, authorizedFunctionalityForTrend);
        } catch (Exception e) {
            fail("Failed to set authorizedFunctionalityForTrend field");
        }
        
        boolean result = contractController.isContractHasFunctionalityAllowingTrend(contract);
        assertFalse(result);
    }

    @Test
    public void testIsContractHasFunctionalityAllowingManualAudit_True() {
        Contract contract = new Contract();
        Set<Functionality> functionalitySet = new HashSet<>();
        Functionality functionality = new Functionality();
        functionality.setId(5L);
        functionalitySet.add(functionality);
        contract.setFunctionalitySet(functionalitySet);
        
        boolean result = contractController.isContractHasFunctionalityAllowingManualAudit(contract);
        assertTrue(result);
    }

    @Test
    public void testIsContractHasFunctionalityAllowingManualAudit_False() {
        Contract contract = new Contract();
        Set<Functionality> functionalitySet = new HashSet<>();
        Functionality functionality = new Functionality();
        functionality.setId(10L);
        functionalitySet.add(functionality);
        contract.setFunctionalitySet(functionalitySet);
        
        boolean result = contractController.isContractHasFunctionalityAllowingManualAudit(contract);
        assertFalse(result);
    }

    @Test
    public void testIsUserOwnedContract_True() {
        Contract contract = new Contract();
        contract.setId(1L);
        List<Contract> contracts = Arrays.asList(contract);
        when(contractDataService.getAllContractsByUser(any())).thenReturn(contracts);
        
        boolean result = contractController.isUserOwnedContract(1L);
        assertTrue(result);
    }

    @Test
    public void testIsUserOwnedContract_False() {
        Contract contract = new Contract();
        contract.setId(1L);
        List<Contract> contracts = Arrays.asList(contract);
        when(contractDataService.getAllContractsByUser(any())).thenReturn(contracts);
        
        boolean result = contractController.isUserOwnedContract(2L);
        assertFalse(result);
    }
}
