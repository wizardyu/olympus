package com.wizardyu.search.test;
 
import javax.annotation.Resource;

import org.springframework.stereotype.Controller;  
  
import org.springframework.web.bind.annotation.RequestMapping;  
  
import org.springframework.web.bind.annotation.RequestMethod;  
  
import org.springframework.web.servlet.ModelAndView;

import com.wizardyu.olympus.hera.search.LuceneService;


  
   
  
@Controller  
public class TestController {  
  
	@Resource(name="luceneService")
	private LuceneService luceneService;
      
    public TestController() {  
        System.out.println("TestController constructed......");  
    }  
  
      
    @RequestMapping(value="/test",method=RequestMethod.GET)  
    public ModelAndView testMVC(){  
        ModelAndView modelAndView = new ModelAndView("test");  
        modelAndView.addObject("info", luceneService.test());  
        return modelAndView;  
    }  
}  