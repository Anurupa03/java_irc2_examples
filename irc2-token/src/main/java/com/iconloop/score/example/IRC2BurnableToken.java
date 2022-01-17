/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.example;


import score.Context;

import java.math.BigInteger;

public class IRC2BurnableToken extends IRC2Burnable {
    public IRC2BurnableToken(String name, String symbol, int decimals, BigInteger initialSupply) {
        super(name, symbol, decimals);

        // mint the initial token supply here
        Context.require(initialSupply.compareTo(BigInteger.ZERO) >= 0);
        _mint(Context.getCaller(), initialSupply.multiply(pow10(decimals)));
    }

    private static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }
        return result;
    }


}
