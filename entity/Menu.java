package inetbas.web.outsys.entity;

import java.io.Serializable;
import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;


/**
 * Created by www.bip-soft.com on 2017/5/17.
 */
public class Menu implements Serializable {
    private String menuId;
    private String menuName;
    private String menuIcon;
    private String command;
    private int menuattr;
    private ArrayList<Menu> childMenu;

    private boolean isTop = false;

    @JSONField(serialize=false)
    private Menu parentMenu;

    private boolean haveChild;

    public Menu(String menuId, String menuName) {
        this.menuId = menuId;
        this.menuName = menuName;
        
    }

    public Menu(String menuId, String menuName, String cmd,String menuIcon) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.command = cmd;
        this.menuIcon = menuIcon;

    }
    @JSONField(name="menuId")
    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }
    @JSONField(name="menuName")
    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
    @JSONField(name="menuIcon")
    public String getMenuIcon() {
        return menuIcon;
    }

    public void setMenuIcon(String menuIcon) {
        this.menuIcon = menuIcon;
    }
    @JSONField(name="command")
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
    @JSONField(name="childMenu")
    public ArrayList<Menu> getChildMenu() {
        return childMenu;
    }

    public void setChildMenu(ArrayList<Menu> childMenu) {
        this.childMenu = childMenu;
    }

    public void addChild(Menu menu) {
        if (childMenu == null)
            childMenu = new ArrayList<Menu>();
     
        if(this.menuId=="")
              menu.	setTop(true);
        menu.setParentMenu(this);
        haveChild = true;
        childMenu.add(menu);
    }


    @JSONField(name="haveChild")
    public boolean isHaveChild() {
        return haveChild;
    }

    public void setHaveChild(boolean haveChild) {
        this.haveChild = haveChild;
    }
    
    public Menu getParentMenu() {
        return parentMenu;
    }

    public void setParentMenu(Menu parentMenu) {
        this.parentMenu = parentMenu;
    }

	public boolean isTop() {
		return isTop;
	}

	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}

	public int getMenuattr() {
		return menuattr;
	}

	public void setMenuattr(int menuattr) {
		this.menuattr = menuattr;
	}
}
