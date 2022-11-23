package com.phoenix.shuaidatabase.single;

public enum ShuaiEliminateStrategy {

    NOEVICTION,

    ALLKEYS_LRU,

    VOLATILE_LRU,

    ALLKEYS_RANDOM,

    VOLATILE_RANDOM,

    VOLATILE_TTL,

    LSM_TREE;
}
