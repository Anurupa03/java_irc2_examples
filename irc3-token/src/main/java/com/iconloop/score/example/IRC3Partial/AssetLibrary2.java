package com.iconloop.score.example;

import score.Context;

import java.math.BigInteger;

public class AssetLibrary2 {

    public class Asset{
        protected BigInteger slot;
        protected BigInteger unit;
        protected boolean isValid;
    }

    public void mint(Asset self, BigInteger slots, BigInteger units){
        if (!self.isValid) {
            self.slot = slots;
            self.isValid = true;
        } else {
            Context.require(slots.equals(self.slot), "slot mismatch");
        }
        units = self.unit.add(self.unit);
    }

    public BigInteger merge(Asset self, Asset target){
        Context.require(self.isValid && target.isValid, "asset not exists");
        Context.require(self.slot.equals(target.slot), "slot mismatch");

        BigInteger mergeUnits = self.unit;
        self.unit = self.unit.subtract(mergeUnits);
        self.isValid = false;

        return mergeUnits;
    }

    public void transfer(Asset self, Asset target, BigInteger units){
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

    public void burn(Asset self, BigInteger units){
        self.unit = self.unit.subtract(units);
    }


}
