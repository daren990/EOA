package cn.oa.service;

import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.model.AddressMenu;
import cn.oa.model.AddressUser;

@IocBean
public class AddressBookService {
	@Inject
	private Dao dao;
	public void saveGroup(final AddressMenu amAddressMenu ){
		dao.insert(amAddressMenu);
	}	
	
	public List<AddressMenu> findGroup(Integer userId) {
		return dao.query(AddressMenu.class, Cnd.where("user_id","=",userId));
	}
	public void saveUser(final AddressUser addressUser){
				dao.insert(addressUser);
	}
	public AddressUser findUser(Integer userId){
		return dao.fetch(AddressUser.class,userId);
	}
	public AddressMenu findMenu(Integer userId){
		return dao.fetch(AddressMenu.class,userId);
	}
	public void updateUser(AddressUser addressUser){
		dao.update(addressUser);
	}
	public void delUser(Integer id){
		dao.delete(AddressUser.class,id);
	}
	public void delGroup(Integer id){
		dao.delete(AddressMenu.class,id);
	}
	public void updateMenu(AddressMenu addressMenu){
		dao.update(addressMenu);
	}
}
