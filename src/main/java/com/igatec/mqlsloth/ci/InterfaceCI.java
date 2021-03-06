package com.igatec.mqlsloth.ci;

import com.igatec.mqlsloth.ci.annotation.ModStringSetProvider;
import com.igatec.mqlsloth.ci.constants.CIDiffMode;
import com.igatec.mqlsloth.ci.constants.SlothAdminType;
import com.igatec.mqlsloth.ci.util.CIFullName;
import com.igatec.mqlsloth.script.ModChunk;
import com.igatec.mqlsloth.script.MqlUtil;
import com.igatec.mqlsloth.script.ScriptChunk;
import com.igatec.mqlsloth.util.ReversibleSet;
import com.igatec.mqlsloth.util.SlothSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InterfaceCI extends TypeLikeCI {

    private ReversibleSet<String> types;
    private ReversibleSet<String> relationships;

    public InterfaceCI(String name) {
        this(name, CIDiffMode.TARGET);
    }
    public InterfaceCI(String name, CIDiffMode diffMode) {
        super(SlothAdminType.INTERFACE, name, diffMode);
        if (getDiffMode() == CIDiffMode.TARGET) {
            initTarget();
        } else {
            initDiff();
        }
    }

    private void initTarget(){
        types = new SlothSet<>(false);
        relationships = new SlothSet<>(false);
    }

    private void initDiff(){
        types = new SlothSet<>(true);
        relationships = new SlothSet<>(true);
    }

    @ModStringSetProvider(value = M_TYPE, addPriority = SP_ADD_TYPE_TO)
    public ReversibleSet<String> getTypes(){
        return new SlothSet<>(types, isDiffMode());
    }
    public boolean addType(String type){
        return types.add(type);
    }
    public boolean reverseType(String type){
        checkModeAssertion(CIDiffMode.DIFF);
        return types.reverse(type);
    }

    @ModStringSetProvider(value = M_RELATIONSHIP, addPriority = SP_ADD_REL_TO)
    public ReversibleSet<String> getRelationships(){
        return new SlothSet<>(relationships, isDiffMode());
    }
    public boolean addRelationship(String rel){
        return relationships.add(rel);
    }
    public boolean reverseRelationship(String rel){
        checkModeAssertion(CIDiffMode.DIFF);
        return relationships.reverse(rel);
    }

    @Override
    protected void fillDiffCI(AbstractCI newCI, AbstractCI diffCI){
        super.fillDiffCI(newCI, diffCI);
        InterfaceCI newCastedCI = (InterfaceCI) newCI;
        InterfaceCI diffCastedCI = (InterfaceCI) diffCI;
        {
            ReversibleSet<String> oldValues = getTypes();
            ReversibleSet<String> newValues = newCastedCI.getTypes();
            for (String value:SlothSet.itemsToRemove(oldValues, newValues))
                diffCastedCI.reverseType(value);
            for (String value:SlothSet.itemsToAdd(oldValues, newValues))
                diffCastedCI.addType(value);
        }
        {
            ReversibleSet<String> oldValues = getRelationships();
            ReversibleSet<String> newValues = newCastedCI.getRelationships();
            for (String value:SlothSet.itemsToRemove(oldValues, newValues))
                diffCastedCI.reverseRelationship(value);
            for (String value:SlothSet.itemsToAdd(oldValues, newValues))
                diffCastedCI.addRelationship(value);
        }
    }

    @Override
    public InterfaceCI buildDiff(AbstractCI newCI){
        InterfaceCI ci = (InterfaceCI) newCI;
        InterfaceCI diff = new InterfaceCI(getName(), CIDiffMode.DIFF);
        fillDiffCI(ci, diff);
        return diff;
    }

    @Override
    public AbstractCI buildDefaultCI(){
        return new InterfaceCI(getName());
    }

    public boolean isEmpty(){
        if (!super.isEmpty())
            return false;
        return types.isEmpty() && relationships.isEmpty();
    }


    public Map<String, Object> toMap(){
        Map<String, Object> fieldsValues = super.toMap();

        fieldsValues.put(Y_TYPES, getTypes());
        fieldsValues.put(Y_RELATIONSHIPS, getRelationships());

        return fieldsValues;
    }
}
