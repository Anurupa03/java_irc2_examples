package com.iconloop.score.example;

import score.Context;

import java.math.BigInteger;

public class AssetFunction extends IRC3{

    public AssetFunction(String _name, String _symbol) {
        super(_name, _symbol);
    }

    public void mint(AssetLibrary self, BigInteger slots, BigInteger units) {
        if (!self.isValid) {
            self.slot = slots;
            self.isValid = true;
        } else {
            Context.require(slots.equals(self.slot), "slot mismatch");
        }
        units = self.unit.add(self.unit);
    }

    public BigInteger merge(AssetLibrary self, AssetLibrary target){
        Context.require(self.isValid && target.isValid, "asset not exists");
        Context.require(self.slot.equals(target.slot), "slot mismatch");

        BigInteger mergeUnits = self.unit;
        self.unit = self.unit.subtract(mergeUnits);
        self.isValid = false;

        return mergeUnits;
    }

    public void transfer(AssetLibrary self, AssetLibrary target, BigInteger units){
        Context.require(self.isValid,"asset not exits");
        self.unit = self.unit.subtract(units);
        if (target.isValid){
            Context.require(self.slot.equals(target.slot),"slot mismatch");
        }
        else {
            target.slot = self.slot;
            target.isValid = true;
        }

        target.unit = target.unit.add(units);
    }

    public void burn(AssetLibrary self, BigInteger units){
        self.unit = self.unit.subtract(units);
    }
}
