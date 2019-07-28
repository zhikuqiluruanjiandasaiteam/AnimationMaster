package com.example.demo.realm;


import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;


public class MyRealm extends AuthorizingRealm{

	@Autowired
	private UserService userService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		return null;
	}

	/**
	 * 权限认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String userName=(String)token.getPrincipal();
		User user=userService.findByUserName(userName);
		if(user!=null){
			AuthenticationInfo authcInfo=new SimpleAuthenticationInfo(user.getUserName(),user.getPassword(),"xx");
			return authcInfo;
		}else{
			return null;			
		}
	}

}
