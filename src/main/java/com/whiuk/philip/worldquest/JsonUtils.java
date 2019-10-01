package com.whiuk.philip.worldquest;

import org.json.simple.JSONObject;

import java.awt.*;

public class JsonUtils {
    static int intFromObj(Object o) {
        return ((Long) o).intValue();
    }

    static Color parseColor(JSONObject colorData) {
        return new Color(intFromObj(colorData.get("r")), intFromObj(colorData.get("g")), intFromObj(colorData.get("b")));
    }
}
