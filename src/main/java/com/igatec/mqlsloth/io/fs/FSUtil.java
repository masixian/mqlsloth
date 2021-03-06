package com.igatec.mqlsloth.io.fs;

import com.igatec.mqlsloth.ci.ProgramCI;
import com.igatec.mqlsloth.ci.constants.SlothAdminType;
import com.igatec.mqlsloth.ci.util.AbstractCIName;
import com.igatec.mqlsloth.ci.util.BusCIName;
import com.igatec.mqlsloth.ci.util.CIFullName;
import com.igatec.mqlsloth.ci.util.StringCIName;
import com.igatec.mqlsloth.iface.io.PersistenceFormat;
import com.igatec.mqlsloth.util.BiMap;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FSUtil {

    public final static String JPO_FILE_NAME_END = "_mxJPO.java";

    private final static String REV_DELIMITER = "_rev_";
    private final static String REV_DELIMITER_PATTERN = "_rev_";

    final static String ADMIN_BUS_DIR = "adminBus";
    final static String DATA_MODEL_DIR = "dataModel";
    final static String PAGE_DIR = "page";
    final static String PROGRAM_DIR = "program";
    final static String USER_DIR = "user";
    final static String UI_DIR = "ui";

    final static BiMap<SlothAdminType, String> relPaths = new BiMap<>();
    final static BiMap<PersistenceFormat, String> fileExtensions = new BiMap<>();

    static {
        relPaths.put(SlothAdminType.TYPE, buildPath(DATA_MODEL_DIR, "type"));
        relPaths.put(SlothAdminType.ATTRIBUTE, buildPath(DATA_MODEL_DIR, "attribute"));
        relPaths.put(SlothAdminType.INTERFACE, buildPath(DATA_MODEL_DIR, "interface"));
        relPaths.put(SlothAdminType.RELATIONSHIP, buildPath(DATA_MODEL_DIR, "relationship"));
        relPaths.put(SlothAdminType.POLICY, buildPath(DATA_MODEL_DIR, "policy"));

        relPaths.put(SlothAdminType.PAGE, buildPath(PAGE_DIR));

        relPaths.put(SlothAdminType.PROGRAM, buildPath(PROGRAM_DIR));

        relPaths.put(SlothAdminType.NUMBER_GENERATOR, buildPath(ADMIN_BUS_DIR, "ng"));
        relPaths.put(SlothAdminType.OBJECT_GENERATOR, buildPath(ADMIN_BUS_DIR, "og"));
        relPaths.put(SlothAdminType.TRIGGER, buildPath(ADMIN_BUS_DIR, "trigger"));

        relPaths.put(SlothAdminType.ROLE, buildPath(USER_DIR, "role"));
        relPaths.put(SlothAdminType.GROUP, buildPath(USER_DIR, "group"));

        relPaths.put(SlothAdminType.COMMAND, buildPath(UI_DIR, "command"));
        relPaths.put(SlothAdminType.MENU, buildPath(UI_DIR, "menu"));
        relPaths.put(SlothAdminType.CHANNEL, buildPath(UI_DIR, "channel"));
        relPaths.put(SlothAdminType.PORTAL, buildPath(UI_DIR, "portal"));
        relPaths.put(SlothAdminType.EXPRESSION, buildPath(DATA_MODEL_DIR, "expression"));
        relPaths.put(SlothAdminType.FORM, buildPath(UI_DIR, "form"));
        relPaths.put(SlothAdminType.WEB_TABLE, buildPath(UI_DIR, "table"));

        /* For future
        relPaths.put(SlothAdminType.DIMENSION, buildPath(DATA_MODEL_DIR, "dimension"));
        */

        fileExtensions.put(PersistenceFormat.YML, "yml");
    }

    static String buildContentFileDir(String baseDir, SlothAdminType aType, ProgramCI.Type programType){
        String contentDir = null;
        if (aType == SlothAdminType.PROGRAM){
            if (programType == ProgramCI.Type.JAVA)
                contentDir = "jpo";
        } else if (aType == SlothAdminType.PAGE){
            contentDir = "content";
        }
        if (contentDir == null)
            return null;
        return FSUtil.buildPath(baseDir, FSUtil.relPaths.getValue(aType), contentDir);
    }

    static String buildContentFilePath(String baseDir, CIFullName fullName, ProgramCI.Type programType){
        SlothAdminType aType = fullName.getAdminType();
        String contentDir = buildContentFileDir(baseDir, aType, programType);
        if (contentDir == null)
            return null;
        if (aType == SlothAdminType.PROGRAM){
            if (programType == ProgramCI.Type.JAVA)
                return FSUtil.buildPath(contentDir, fullName.getName() + JPO_FILE_NAME_END);
        } else if (aType == SlothAdminType.PAGE){
            return FSUtil.buildPath(contentDir, fullName.getName());
        }
        return null;
    }

    static String buildPath(String... items){
        return Arrays.stream(items).reduce((s, s2) -> s + File.separator + s2).get();
    }

    static String buildFileName(CIFullName ciFullName, PersistenceFormat format){
        String fileName;
        AbstractCIName ciName = ciFullName.getCIName();
        if (ciName instanceof StringCIName){
            fileName = ciName.toString();
        } else if (ciName instanceof BusCIName){
            BusCIName tnr = (BusCIName) ciName;
            fileName = tnr.getName() + REV_DELIMITER + tnr.getRevision();
        } else {
            fileName = ciName.toString(); // This should not happen
        }
        fileName += "." + fileExtensions.getValue(format);
        return fileName;
    }

    static CIFullName buildNameByFileNameIfMatch(SlothAdminType aType, String fileName){
        String noExtFileName = fileName.substring(0, fileName.lastIndexOf("."));
        AbstractCIName ciName;
        if (SlothAdminType.isBus(aType)){
            String[] arr = noExtFileName.split(REV_DELIMITER_PATTERN, -1);
            if (arr.length != 2)
                return null;
            ciName = new BusCIName(aType.getMqlBusType(), arr[0], arr[1]);
        } else {
            ciName = new StringCIName(noExtFileName);
        }
        return new CIFullName(aType, ciName);
    }

    static boolean matchesPattern(CIFullName name, String pattern){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(name.getName());
        return m.matches();
    }

    /*
        Special symbols: '*' (any number of any symbols)
        Real MQL parser also includes '?', but it is used rarely
    */
    public static boolean matchesMqlPattern(CIFullName fullName, String pattern){
        final String DUMMY = "D";
        String name = fullName.getName();

        if (name.equals(pattern))
            return true;

        if (pattern.startsWith("*")){
            pattern = DUMMY + pattern;
            name = DUMMY + name;
        }
        if (pattern.endsWith("*")){
            pattern = pattern + DUMMY;
            name = name + DUMMY;
        }
        String[] pItems = pattern.split("\\*");

        if (!name.startsWith(pItems[0]))
            return false;
        name = name.substring(pItems[0].length());

        for (int i=1; i<pItems.length; i++){
            String pItem =pItems[i];
            int index;
            if (i < pItems.length-1)
                index = name.indexOf(pItem);
            else
                index = name.lastIndexOf(pItem);
            if (index == -1)
                return false;
            name = name.substring(index + pItem.length());
        }

        return "".equals(name);

    }

}
