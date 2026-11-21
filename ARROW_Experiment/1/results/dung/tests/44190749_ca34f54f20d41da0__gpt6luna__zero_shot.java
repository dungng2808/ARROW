package org.asqatasun.webapp.controller;

import java.util.Collections;
import java.util.Locale;
import org.asqatasun.entity.contract.Contract;
import org.asqatasun.entity.service.contract.ContractDataService;
import org.asqatasun.webapp.dto.factory.DetailedContractInfoFactory;
import org.asqatasun.webapp.exception.ForbiddenPageException;
import org.asqatasun.webapp.exception.ForbiddenUserException;
import org.asqatasun.webapp.util.TgolKeyStore;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContractController_ArrowTest {

    @Test
    public void invalidContractIdThrowsForbiddenPageException() {
        ContractDataService service = mock(ContractDataService.class);
        DetailedContractInfoFactory factory = mock(DetailedContractInfoFactory.class);
        LocaleResolver resolver = mock(LocaleResolver.class);
        ContractController controller = new ContractController(service, factory, resolver);

        try {
            controller.displayContractPage("not-a-number", mock(HttpServletRequest.class), new ExtendedModelMap());
            fail("Expected ForbiddenPageException");
        } catch (ForbiddenPageException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void contractNotOwnedThrowsForbiddenUserException() {
        ContractDataService service = mock(ContractDataService.class);
        when(service.getAllContractsByUser(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.<Contract>emptyList());
        DetailedContractInfoFactory factory = mock(DetailedContractInfoFactory.class);
        LocaleResolver resolver = mock(LocaleResolver.class);
        ContractController controller = new ContractController(service, factory, resolver);

        try {
            controller.displayContractPage("1", mock(HttpServletRequest.class), new ExtendedModelMap());
            fail("Expected ForbiddenUserException");
        } catch (ForbiddenUserException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void ownedContractDisplaysContractPage() {
        ContractDataService service = mock(ContractDataService.class);
        Contract contract = mock(Contract.class);
        when(contract.getId()).thenReturn(1L);
        when(service.getAllContractsByUser(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.singletonList(contract));
        when(service.read(1L)).thenReturn(contract);
        when(contract.getFunctionalitySet()).thenReturn(Collections.emptySet());
        when(contract.getId()).thenReturn(1L);

        DetailedContractInfoFactory factory = mock(DetailedContractInfoFactory.class);
        when(factory.getDetailedContractInfo(contract)).thenReturn(null);
        LocaleResolver resolver = mock(LocaleResolver.class);
        when(resolver.resolveLocale(org.mockito.ArgumentMatchers.any(HttpServletRequest.class))).thenReturn(Locale.ENGLISH);

        ContractController controller = new ContractController(service, factory, resolver);
        Model model = new ExtendedModelMap();

        assertEquals(TgolKeyStore.CONTRACT_VIEW_NAME,
                controller.displayContractPage("1", mock(HttpServletRequest.class), model));
        assertEquals(1L, model.asMap().get(TgolKeyStore.CONTRACT_ID_VALUE));
        assertEquals(Locale.ENGLISH, model.asMap().get(TgolKeyStore.LOCALE_KEY));
        assertEquals(Boolean.FALSE, model.asMap().get(TgolKeyStore.IS_CONTRACT_EXPIRED_KEY));
    }
}
