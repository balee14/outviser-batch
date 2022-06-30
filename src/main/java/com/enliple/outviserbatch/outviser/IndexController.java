package com.enliple.outviserbatch.outviser;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public void index(HttpServletResponse res) throws Exception {

		res.setContentType("text/html");
		res.setCharacterEncoding("utf-8");
		res.getWriter().print("outviser-batch_v1.4");
	}
}
