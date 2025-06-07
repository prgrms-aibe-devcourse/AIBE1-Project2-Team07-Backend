package org.lucky0111.pettalk.controller.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public abstract class ControllerTest {
    @Autowired
    protected MockMvc mockMvc;
}
