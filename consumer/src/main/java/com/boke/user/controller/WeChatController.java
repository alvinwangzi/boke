package com.boke.user.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dahua.boke.entity.User;
import com.dahua.boke.entity.WeChat;
import com.dahua.boke.service.WeChatService;
import com.dahua.boke.util.BokeUtil;

@RestController()
@RequestMapping("wechat")
public class WeChatController {
	
	private static Logger logger = LoggerFactory.getLogger(WeChatController.class);
	
	@Autowired
	private WeChatService weChatService;

	/**
	 * 获取微信发过来的数据，解析,验证
	 */
	@GetMapping("/getMes")
	public String getWeChat(HttpServletRequest request){
		/*boolean isGet = request.getMethod().toLowerCase().equals("get");  */
		String signature = request.getParameter("signature");  
		String timestamp = request.getParameter("timestamp");  
		String nonce = request.getParameter("nonce");  
		String echostr = request.getParameter("echostr");  
		logger.info("signature - "+signature+" timestamp - "+timestamp+" nonce - "+nonce+" echostr - "+echostr);
		return weChatService.Verification(new WeChat(signature,timestamp,nonce,echostr));
	} 
	
	/**
	 * 获取微信发过来的数据，解析,验证
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	@PostMapping("/getMes")
	public void getWeChatAndReply(HttpServletRequest request,HttpServletResponse response){
		try {
			/**
			 * 这里的编码格式设置其主要作用
			 */
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // 调用核心业务类接收消息、处理消息  
        User u = weChatService.processRequest(request,response);  
        if(u.getName()!=null) {
	    	Session session=BokeUtil.getSession();
			SecurityUtils.getSubject().getSession().setTimeout(600000);
			// 令牌验证登陆
			SecurityUtils.getSubject().login(new UsernamePasswordToken(u.getName(), u.getPassword()));
			session.setAttribute("user", u);
        }
        // 响应消息  
	} 
	
	/**
	 * 新建微信自定义菜单
	 */
	@RequestMapping("/createMenu")
	public String CreateMenu(){
		return weChatService.CreateMenu();
	}
	
}
