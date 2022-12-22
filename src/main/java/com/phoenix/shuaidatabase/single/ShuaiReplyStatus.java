package com.phoenix.shuaidatabase.single;

import java.io.Serializable;

public enum ShuaiReplyStatus implements Serializable {

    OK,

    INPUT_FAULT,

    INNER_FAULT,

    OUT_OF_MEMORY,

    WAIT_FOR_REPLY;
}
