package com.boke.user.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dahua.boke.entity.Article;
import com.dahua.boke.entity.Comment;
import com.dahua.boke.entity.MyPhoto;
import com.dahua.boke.entity.User;
import com.dahua.boke.service.UserService;
import com.dahua.boke.service.WeChatService;
import com.dahua.boke.util.BokeUtil;
import com.dahua.boke.util.WeChatMesUtil;

/**
 * 跳转到index主页
 * @author 丁伟强
 *
 */
@Controller  
public class HtmlController {  
	
	@Autowired
	private UserService userService;
	@Autowired 
	private WeChatService weChatService;
	private static Logger logger = LoggerFactory.getLogger(HtmlController.class);
	/**
	 * 默认首页
	 * @param model
	 * @return
	 */
	@RequestMapping("/")
	public String toIndex(Model model,HttpServletRequest request) {
		String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"newindex")){
			return "loginToMuch";
		}
		Subject subject=SecurityUtils.getSubject();
		Session session=subject.getSession();
	    String time = BokeUtil.getStringTime();
		User newUser = (User) session.getAttribute("user");
		if(newUser == null) {
			userService.saveIP(ipAddress,time,0,null);
		}else {
			userService.saveIP(ipAddress,time,newUser.getId(),newUser.getName());
		}
		return "newindex";
	}
	/**
	 * 访问index页面后台跳转方法
	 * @return
	 */
	@RequestMapping("/index") 
    public String ToIndex(Model model,HttpServletRequest request) {
		String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"index")){
			return "loginToMuch";
		}
		
		//分页下一页功能(废弃)
//		Subject subject=SecurityUtils.getSubject();
//		Session session=subject.getSession();
//		session.setAttribute("nowPage",2);
		//获取全部页给前端，便于点击下一页时候判断是否到了最后一页
		Subject subject=SecurityUtils.getSubject();
		Session session=subject.getSession();
		session.setAttribute("allPage",userService.pageNum());
		User user = (User) session.getAttribute("user");
		if(user!=null){
			model.addAttribute("rowId",user.getRowId()+"");
		}else{
			model.addAttribute("rowId","2");
		}
        return "index";  
    } 
	
	@RequestMapping("images")
	public String images(HttpServletRequest request) {
		String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"user_article")){
			return "loginToMuch";
		}
		return "images";
	}
	
	/**
	 * 访问文章详情页面后台跳转方法
	 * @return
	 */
	@RequestMapping("/single") 
    public String ToSingel(Model model,String id,HttpServletRequest request) { 
		String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"single")){
			return "loginToMuch";
		}
		//根据id查询单条文章的所有信息
		Article article = userService.toSingle(id);
		userService.setSee(id);
		//根据id查询关联的所有评价
		List<Comment> commentList= userService.select_message(id);
		model.addAttribute("article", article);
		model.addAttribute("commentList", commentList);
        return "single";  
    } 
	
	/**
	 * 访问文章编辑页面后台跳转方法
	 * @return
	 */
	@RequestMapping("/write") 
    public String ToWrite(Model model,String type,HttpServletRequest request) { 
		String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"write")){
			return "loginToMuch";
		}
		model.addAttribute("type", type);
		return "write";  
    } 
	
	/**
	 * 访问个人中心页面后台跳转方法
	 * @return
	 */
	@RequestMapping("/myworld") 
    public String myWorld(Model model,HttpServletRequest request) { 
		String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"myworld")){
			return "loginToMuch";
		}
		
 		String modify = request.getParameter("modify");
		String articleId = request.getParameter("articleId");
		
		Integer userId = null;
		String nickname = null;
		int Identification = 0;
		
		if(articleId != null) {
			nickname = userService.toSingle(articleId).getCreate_user();
			model.addAttribute("articleId",articleId);
		}else {
			model.addAttribute("articleId","no");
		}
		
		Subject subject=SecurityUtils.getSubject();
		Session session=subject.getSession();
		User user = (User) session.getAttribute("user");
		
		if(modify == null && user == null) {
			return "login";
		}
		
		if(((modify != null && modify.equals("no") && user == null) || modify != null && !"".equals(modify) && nickname != null && !"".equals(nickname) && !nickname.equals(user.getNickname()))) {
			User nickname_user = new User();
			nickname_user.setNickname(nickname);
			userId = Integer.parseInt(userService.user_seNickname(nickname_user));
			Identification = 1;
		}else {
			//从session中获取当前登陆人昵称
			nickname = user.getNickname();
			userId = user.getId();
			Identification = 2;
		}
		List<Article> mylist = userService.select_article_user_all(nickname);
		model.addAttribute("userId",userId);
		model.addAttribute("list",mylist);
		List<MyPhoto> myPhotoList = userService.select_all(userId);
		List<HashMap<String,String>> image_list = new ArrayList<HashMap<String, String>>();
		if(myPhotoList.size() > 0) {
			for(MyPhoto myPhoto:myPhotoList) {
				String image = "http://www.loveding.top:8089/"+userId + "/" + myPhoto.getPhoto();
				HashMap<String,String> map = new HashMap<String, String>();
				map.put("image", image);
				map.put("text", myPhoto.getPhoto_test());
				image_list.add(map);
			}
			model.addAttribute("image_list",image_list);
		}
		List<String> html = new ArrayList<String>();
		if(Identification == 1) {
			model.addAttribute("modify",modify);
			String newnickname = userService.select_user(userId).getNickname();
			for(int i=0;i<newnickname.length();i++) {
				html.add(String.valueOf(newnickname.charAt(i)));
			}
			model.addAttribute("newnickname",html);
			model.addAttribute("nickname",newnickname);
		}else if(Identification == 2) {
			model.addAttribute("modify","yes");
			for(int i=0;i<nickname.length();i++) {
				html.add(String.valueOf(nickname.charAt(i)));
			}
			model.addAttribute("newnickname",html);
			model.addAttribute("nickname",nickname);
		}
		return "myword";  
    } 
	
	/**
	 * 访问注册帐号页面后台跳转方法
	 * @return
	 */
	@RequestMapping("/register") 
    public String ToRegister(Model model,HttpServletRequest request) {  
		String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"login")){
			return "loginToMuch";
		}
		return "login";  
    } 
	
	/**
	 * 访问login页面后台跳转方法，shiro添加支持
	 * @return
	 */
    @RequestMapping(value="/login", method = RequestMethod.GET)
	    public String ToLogin(Model model,HttpServletRequest request) {
    	String code = request.getParameter("code");//这里的code是微信跳转后自动跳转的页面带的参数，不是自己附上去的
    	String state = request.getParameter("state");//这里的state是自己附上去跳转的链接
    	System.out.println("state-------------------------------------------------------"+state);
    	System.out.println("code-------------------------------------------------------"+code);
		if(code!=null){
			User u = weChatService.weChatLogin(code);
			if(u.getName()!=null) {
		    	Session session=BokeUtil.getSession();
				SecurityUtils.getSubject().getSession().setTimeout(600000);
				// 令牌验证登陆
				SecurityUtils.getSubject().login(new UsernamePasswordToken(u.getName(), u.getPassword()));
				session.setAttribute("user", u);
	        }
			if("write1".equals(state)){
				return ToWrite(model,"1",request);
			}else if("write2".equals(state)){
				return ToWrite(model,"2",request);
			}else if("write3".equals(state)){
				return ToWrite(model,"3",request);
			}else if("index".equals(state)){
				return ToIndex(model,request);
			}else if("myworld".equals(state)){
				return myWorld(model,request);
			}else if("images".equals(state)){
				return images(request);
			}
		}else{
			String ipAddress = BokeUtil.getIP(request);
			if(!userService.visit(ipAddress,"loginToMuch")){
				return "loginToMuch";
			}
		}
	        return "login";
	    }

	/**
	 * 用户注销
	 */
    @RequestMapping("/loginOut")
    public String loginOut(Model model,HttpServletRequest request){
    	Subject subject=SecurityUtils.getSubject();
    	subject.logout();
    	return ToLogin(model,request);
    }
    
    /**
 	    * 用户扫码登录后跳转页面
     */
    @RequestMapping("/Sweep")
    public String sweep(Model model,HttpServletRequest request,HttpServletResponse response){
    	String loginUUID = request.getParameter("loginUUID");
    	String state = request.getParameter("state");
    	String code = request.getParameter("code");//这里的code是微信跳转后自动跳转的页面带的参数，不是自己附上去的
    	String loginUrl = WeChatMesUtil.WeChat_Saoma_URL;
    	logger.debug(loginUUID+"Sweep++++++++++++++++++"+state+"++++++++++++++++++++First"+code);
    	if(loginUUID ==null && !"".equals(loginUUID) && code!=null && !"".equals(code) && state !=null && !"".equals(state)) {
    		logger.debug("Sweep++++++++++++++++++++++++++++++++++++++success");
    		String backState = userService.sweep(state,code);
    		if("success".equals(backState)) {
    			model.addAttribute("mes", "扫码登录成功");
    		}else if("timeout".equals(backState)) {
    			model.addAttribute("mes", "二维码已过期");
    		}else {
    			model.addAttribute("mes", "登录失败，错误未知");
    		}
    	}else if(loginUUID!=null && !"".equals(loginUUID) && (code==null || "".equals(code))){
    		logger.debug("Sweep++++++++++++++++++++++++++++++++++++++second");
    		try {
				response.sendRedirect(loginUrl.split("#")[0]+loginUUID+"#"+loginUrl.split("#")[1]);
			} catch (IOException e) {
				logger.debug("Sweep++++++++++++++++++++++++++++++++++++++seconderror");
			}
    	}else {
    		logger.debug("Sweep++++++++++++++++++++++++++++++++++++++error");
    		model.addAttribute("mes", "扫码登录失败");
    	}
    	return "saoma";
    }
    
    /**
	 * 用户文章
	 */
    @RequestMapping("/user_article")
    public String user_article(Model model,String nickname,HttpServletRequest request){
    	String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"user_article")){
			return "loginToMuch";
		}
    	List<Article> list = userService.select_article_user_all(nickname);
    	model.addAttribute("list", list);
    	return "user_article";
    }
    
    /**
     * 访问照片详情页面
     */
    @RequestMapping("/user_photo")
    public String user_photo(Model model,String nickname,HttpServletRequest request){
    	String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"loginToMuch")){
			return "loginToMuch";
		}
    	List<MyPhoto> photos = userService.select_photo_user_all(nickname);
    	for(MyPhoto myPhoto:photos) {
    		myPhoto.setPhoto("http://www.loveding.top:8089/"+myPhoto.getUser_id() + "/" +myPhoto.getPhoto());
    	}
    	model.addAttribute("photos", photos);
    	return "user_photo";
    }
    
    @RequestMapping("/mine")
    public String mine(HttpServletRequest request){
    	String ipAddress = BokeUtil.getIP(request);
		/**
		 * 限制当前ip1分钟内最多访问20次本页面（防爬虫增大服务器压力）
		 */
		if(!userService.visit(ipAddress,"user_article")){
			return "loginToMuch";
		}
    	return "mine";
    }
    
}