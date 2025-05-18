package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Asset;
import com.vermeg.sinistpro.model.Policy;

public class PolicyRequest {

        private Policy policy;
        private Asset asset;

        public Policy getPolicy() {
            return policy;
        }

        public void setPolicy(Policy policy) {
            this.policy = policy;
        }

        public Asset getAsset() {
            return asset;
        }

        public void setAsset(Asset asset) {
            this.asset = asset;
        }

}
