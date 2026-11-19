package org.asqatasun.webapp.controller;

import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.asqatasun.entity.contract.Contract;
import org.asqatasun.entity.service.contract.ContractDataService;
import org.asqatasun.webapp.dto.factory.DetailedContractInfoFactory;
import org.asqatasun.webapp.exception.ForbiddenPageException;
import org.asqatasun.webapp.exception.ForbiddenUserException;
import org.asqatasun.webapp.util.TgolKeyStore;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContractController_ArrowTest {

    @Test
    public void invalidContractIdThrowsForbiddenPageException() {
        ContractDataService contractDataService = mock(ContractDataService.class);
        DetailedContractInfoFactory factory = mock(DetailedContractInfoFactory.class);
        LocaleResolver localeResolver = mock(LocaleResolver.class);
        ContractController controller =
                new ContractController(contractDataService, factory, localeResolver);

        assertThrows(ForbiddenPageException.class, () ->
                controller.displayContractPage("not-a-number", mock(HttpServletRequest.class),
                        new ExtendedModelMap()));
    }

    @Test
    public void contractNotOwnedByUserThrowsForbiddenUserException() {
        ContractDataService contractDataService = mock(ContractDataService.class);
        when(contractDataService.getAllContractsByUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.<Contract>emptyList());
        ContractController controller = new ContractController(
                contractDataService, mock(DetailedContractInfoFactory.class),
                mock(LocaleResolver.class));

        assertThrows(ForbiddenUserException.class, () ->
                controller.displayContractPage("1", mock(HttpServletRequest.class),
                        new ExtendedModelMap()));
    }

    @Test
    public void ownedContractDisplaysContractPage() {
        ContractDataService contractDataService = mock(ContractDataService.class);
        Contract contract = mock(Contract.class);
        when(contract.getId()).thenReturn(1L);
        when(contractDataService.getAllContractsByUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.singletonList(contract));
        when(contractDataService.read(1L)).thenReturn(contract);
        when(contract.getFunctionalitySet()).thenReturn(Collections.emptySet());
        when(contract.getId()).thenReturn(1L);

        DetailedContractInfoFactory factory = mock(DetailedContractInfoFactory.class);
        Object detailedInfo = new Object();
        when(factory.getDetailedContractInfo(contract)).thenReturn(detailedInfo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        Locale locale = Locale.ENGLISH;
        LocaleResolver localeResolver = mock(LocaleResolver.class);
        when(localeResolver.resolveLocale(request)).thenReturn(locale);

        ContractController controller =
                new ContractController(contractDataService, factory, localeResolver);
        Model model = new ExtendedModelMap();

        String view = controller.displayContractPage("1", request, model);

        assertEquals(TgolKeyStore.CONTRACT_VIEW_NAME, view);
        assertEquals(locale, model.asMap().get(TgolKeyStore.LOCALE_KEY));
        assertEquals(1L, model.asMap().get(TgolKeyStore.CONTRACT_ID_VALUE));
        assertEquals(detailedInfo, model.asMap().get(TgolKeyStore.DETAILED_CONTRACT_INFO));
        assertFalse((Boolean) model.asMap().get(TgolKeyStore.IS_CONTRACT_EXPIRED_KEY));
        assertFalse(model.containsAttribute(TgolKeyStore.DISPLAY_RESULT_TREND_KEY));
        assertFalse(model.containsAttribute(TgolKeyStore.CONTRACT_WITH_MANUAL_AUDIT_KEY));
    }
}
