package inetbas.web.outsys.tools;

import inet.HVector;
import inet.Inet;
import inetbas.cli.cutil.CCliTool;
import inetbas.web.outsys.entity.Menu;

import java.util.ArrayList;

import cl.ICL;

/**
 * Created by www.bip-soft.com on 2017/5/17.
 */
public class MenuUtil {

    public static ArrayList<Menu> makeTreeMenu(HVector hh){
        Menu root = new Menu("","ROOT");
        Menu nd0 = root;
        for(int i=0;i<hh.size();i++){
            Object[] menuObj = (Object[]) hh.elementAt(i);
            String menuId = CCliTool.objToString(menuObj[0]);
            String menuName = CCliTool.objToString(menuObj[1]);
            String cmd = CCliTool.objToString(menuObj[2]);
            int  menuAttr = CCliTool.objToInt(menuObj[5],2);
            if(cmd!=null){
            	if(cmd.indexOf("pbuid")>0){
            		cmd = cmd.substring(cmd.indexOf("?")+1)+"&"+ ICL.pmenuid+"="+menuId;
//            		cmd = PTool.byteToString(PTool.encode64(cmd.getBytes()),"",false,false);
//            		cmd = URLEncoder.encode(cmd);
            	}else if(cmd.indexOf("customize")>0){
            		cmd = cmd.substring(cmd.indexOf("?")+1);
            	}else {
					cmd="";
				}
            }
            Menu menuTemp = new Menu(menuId,menuName,cmd);
            menuTemp.setMenuattr(menuAttr);
            while (!menuId.startsWith(Inet.trimLevel(nd0.getMenuId()))) {
                nd0 = nd0.getParentMenu();
            }
            nd0.addChild(menuTemp);
            nd0 = menuTemp;
        }
        return root.getChildMenu();
    }

//    @Test
//    public void test(){
//        HVector hh = new HVector();
//        Object[] oq = new Object[]{"08","营销管理08","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] oo = new Object[]{"093","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] o0 = new Object[]{"09351","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] o1 = new Object[]{"0935101","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] o2 = new Object[]{"0935102","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] o3 = new Object[]{"0935103","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] o4 = new Object[]{"0935104","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] o5 = new Object[]{"0935105","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        Object[] o6 = new Object[]{"0935106","营销管理","mservlet?playout=B:(;I:Guide)&XD_ID=37"};
//        hh.addElement(oq);
//        hh.addElement(oo);
//        hh.addElement(o0);
//        hh.addElement(o1);
//        hh.addElement(o2);
//        hh.addElement(o3);
//        hh.addElement(o4);
//        hh.addElement(o5);
//        hh.addElement(o6);
//        ArrayList<Menu> mm= makeTreeMenu(hh);
//        System.out.println(mm.size());
//    }

}
