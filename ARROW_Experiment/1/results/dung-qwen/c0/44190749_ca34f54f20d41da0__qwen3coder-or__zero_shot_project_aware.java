package org.asqatasun.webapp.controller;

import org.asqatasun.entity.contract.Contract;
import org.asqatasun.entity.functionality.Functionality;
import org.asqatasun.entity.service.contract.ContractDataService;
import org.asqatasun.webapp.dto.factory.DetailedContractInfoFactory;
import org.asqatasun.webapp.exception.ForbiddenPageException;
import org.asqatasun.webapp.exception.ForbiddenUserException;
import org.asqatasun.webapp.util.TgolKeyStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractController_ArrowTest {

    @Mock
    private ContractDataService contractDataService;

    @Mock
    private DetailedContractInfoFactory detailedContractInfoFactory;

    @Mock
    private LocaleResolver localeResolver;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    private ContractController contractController;

    @BeforeEach
    public void setUp() {
        contractController = new ContractController(contractDataService, detailedContractInfoFactory, localeResolver);
    }

    @Test
    public void testDisplayContractPage_InvalidContractId_ShouldThrowForbiddenPageException() {
        assertThrows(ForbiddenPageException.class, () -> {
            contractController.displayContractPage("invalid", request, model);
        });
    }

    @Test
    public void testDisplayContractPage_UserDoesNotOwnContract_ShouldThrowForbiddenUserException() {
        when(contractDataService.getAllContractsByUser(any())).thenReturn(new ArrayList<>());

        assertThrows(ForbiddenUserException.class, () -> {
            contractController.displayContractPage("1", request, model);
        });
    }

    @Test
    public void testDisplayContractPage_ContractExpired_ShouldThrowForbiddenUserException() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setExpirationDate(new Date(System.currentTimeMillis() - 1000));

        when(contractDataService.getAllContractsByUser(any())).thenReturn(Arrays.asList(contract));
        when(contractDataService.read(anyLong())).thenReturn(contract);
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);

        assertThrows(ForbiddenUserException.class, () -> {
            contractController.displayContractPage("1", request, model);
        });
    }

    @Test
    public void testDisplayContractPage_ContractHasFunctionalityAllowingTrend_ShouldAddAttribute() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setExpirationDate(new Date(System.currentTimeMillis() + 1000));

        Functionality functionality = new Functionality();
        functionality.setCode("TREND_FUNCTIONALITY");
        contract.setFunctionalitySet(Collections.singleton(functionality));

        List<String> authorizedFunctionalityForTrend = Arrays.asList("TREND_FUNCTIONALITY");

        // Set the field using reflection since it's private
        try {
            java.lang.reflect.Field field = ContractController.class.getDeclaredField("authorizedFunctionalityForTrend");
            field.setAccessible(true);
            field.set(contractController, authorizedFunctionalityForTrend);
        } catch (Exception e) {
            fail("Failed to set private field");
        }

        when(contractDataService.getAllContractsByUser(any())).thenReturn(Arrays.asList(contract));
        when(contractDataService.read(anyLong())).thenReturn(contract);
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);

        String result = contractController.displayContractPage("1", request, model);

        assertEquals(TgolKeyStore.CONTRACT_VIEW_NAME, result);
        verify(model).addAttribute(eq(TgolKeyStore.DISPLAY_RESULT_TREND_KEY), eq(true));
    }

    @Test
    public void testDisplayContractPage_ContractHasFunctionalityAllowingManualAudit_ShouldAddAttribute() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setExpirationDate(new Date(System.currentTimeMillis() + 1000));

        Functionality functionality = new Functionality();
        functionality.setId(5L);
        contract.setFunctionalitySet(Collections.singleton(functionality));

        when(contractDataService.getAllContractsByUser(any())).thenReturn(Arrays.asList(contract));
        when(contractDataService.read(anyLong())).thenReturn(contract);
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);

        String result = contractController.displayContractPage("1", request, model);

        assertEquals(TgolKeyStore.CONTRACT_VIEW_NAME, result);
        verify(model).addAttribute(eq(TgolKeyStore.CONTRACT_WITH_MANUAL_AUDIT_KEY), eq(true));
    }

    @Test
    public void testIsContractHasFunctionalityAllowingManualAudit_FunctionalityId5_ReturnsTrue() {
        Contract contract = new Contract();
        Functionality functionality = new Functionality();
        functionality.setId(5L);
        contract.setFunctionalitySet(Collections.singleton(functionality));

        boolean result = contractController.isContractHasFunctionalityAllowingManualAudit(contract);
        assertTrue(result);
    }

    @Test
    public void testIsContractHasFunctionalityAllowingManualAudit_FunctionalityIdNot5_ReturnsFalse() {
        Contract contract = new Contract();
        Functionality functionality = new Functionality();
        functionality.setId(6L);
        contract.setFunctionalitySet(Collections.singleton(functionality));

        boolean result = contractController.isContractHasFunctionalityAllowingManualAudit(contract);
        assertFalse(result);
    }

    @Test
    public void testIsContractHasFunctionalityAllowingTrend_FunctionalityInList_ReturnsTrue() {
        Contract contract = new Contract();
        Functionality functionality = new Functionality();
        functionality.setCode("AUTHORIZED_FUNCTIONALITY");
        contract.setFunctionalitySet(Collections.singleton(functionality));

        List<String> authorizedFunctionalityForTrend = Arrays.asList("AUTHORIZED_FUNCTIONALITY");

        try {
            java.lang.reflect.Field field = ContractController.class.getDeclaredField("authorizedFunctionalityForTrend");
            field.setAccessible(true);
            field.set(contractController, authorizedFunctionalityForTrend);
        } catch (Exception e) {
            fail("Failed to set private field");
        }

        boolean result = contractController.isContractHasFunctionalityAllowingTrend(contract);
        assertTrue(result);
    }

    @Test
    public void testIsContractHasFunctionalityAllowingTrend_FunctionalityNotInList_ReturnsFalse() {
        Contract contract = new Contract();
        Functionality functionality = new Functionality();
        functionality.setCode("UNAUTHORIZED_FUNCTIONALITY");
        contract.setFunctionalitySet(Collections.singleton(functionality));

        List<String> authorizedFunctionalityForTrend = Arrays.asList("AUTHORIZED_FUNCTIONALITY");

        try {
            java.lang.reflect.Field field = ContractController.class.getDeclaredField("authorizedFunctionalityForTrend");
            field.setAccessible(true);
            field.set(contractController, authorizedFunctionalityForTrend);
        } catch (Exception e) {
            fail("Failed to set private field");
        }

        boolean result = contractController.isContractHasFunctionalityAllowingTrend(contract);
        assertFalse(result);
    }

    @Test
    public void testIsUserOwnedContract_UserOwnsContract_ReturnsTrue() {
        Contract contract = new Contract();
        contract.setId(1L);

        when(contractDataService.getAllContractsByUser(any())).thenReturn(Arrays.asList(contract));

        boolean result = contractController.isUserOwnedContract(1L);
        assertTrue(result);
    }

    @Test
    public void testIsUserOwnedContract_UserDoesNotOwnContract_ReturnsFalse() {
        Contract contract = new Contract();
        contract.setId(1L);

        when(contractDataService.getAllContractsByUser(any())).thenReturn(Arrays.asList(contract));

        boolean result = contractController.isUserOwnedContract(2L);
        assertFalse(result);
    }

    @Test
    public void testDisplayContractView_AddsCorrectAttributes() {
        Contract contract = new Contract();
        contract.setId(1L);

        when(detailedContractInfoFactory.getDetailedContractInfo(any(Contract.class))).thenReturn("detailedInfo");
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);

        String result = contractController.displayContractView(contract, model);

        assertEquals(TgolKeyStore.CONTRACT_VIEW_NAME, result);
        verify(model).addAttribute(eq(TgolKeyStore.CONTRACT_ID_VALUE), anyLong());
        verify(model).addAttribute(eq(TgolKeyStore.DETAILED_CONTRACT_INFO), anyString());
        verify(model).addAttribute(eq(TgolKeyStore.IS_CONTRACT_EXPIRED_KEY), anyBoolean());
    }
}
