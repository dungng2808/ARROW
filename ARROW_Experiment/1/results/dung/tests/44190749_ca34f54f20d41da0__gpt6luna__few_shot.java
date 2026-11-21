package org.asqatasun.webapp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.asqatasun.entity.contract.Contract;
import org.asqatasun.entity.functionality.Functionality;
import org.asqatasun.entity.service.contract.ContractDataService;
import org.asqatasun.webapp.dto.factory.DetailedContractInfoFactory;
import org.asqatasun.webapp.exception.ForbiddenPageException;
import org.asqatasun.webapp.exception.ForbiddenUserException;
import org.asqatasun.webapp.util.TgolKeyStore;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContractController_ArrowTest {

    @Test
    public void invalidContractIdThrowsForbiddenPageException() {
        ContractController controller = new ContractController(null, null, null);
        try {
            controller.displayContractPage("not-a-number", null, null);
        } catch (ForbiddenPageException expected) {
            assertTrue(true);
            return;
        }
        throw new AssertionError("Expected ForbiddenPageException");
    }

    @Test
    public void validButUnownedContractThrowsForbiddenUserException() {
        ContractDataService service = new ContractDataService() {
            @Override
            public List<Contract> getAllContractsByUser(Object user) {
                return Collections.emptyList();
            }
        };
        ContractController controller = new ContractController(service, null, null);
        try {
            controller.displayContractPage("1", null, null);
        } catch (ForbiddenUserException expected) {
            assertTrue(true);
            return;
        }
        throw new AssertionError("Expected ForbiddenUserException");
    }
}
