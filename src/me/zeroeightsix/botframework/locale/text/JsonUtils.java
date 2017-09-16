package me.zeroeightsix.botframework.locale.text;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by Gebruiker on 17/05/2017.
 */
public class JsonUtils {

    /**
     * Gets the string value of the given JsonElement.  Expects the second parameter to be the name of the element's
     * field if an error message needs to be thrown.
     */
    public static String getString(JsonElement json, String memberName)
    {
        if (json.isJsonPrimitive())
        {
            return json.getAsString();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + toString(json));
        }
    }

    public static <T> T gsonDeserialize(Gson gsonIn, Reader readerIn, Class<T> adapter, boolean lenient)
    {
        try
        {
            JsonReader jsonreader = new JsonReader(readerIn);
            jsonreader.setLenient(lenient);
            return gsonIn.getAdapter(adapter).read(jsonreader);
        }
        catch (IOException ioexception)
        {
            throw new JsonParseException(ioexception);
        }
    }

    public static <T> T gsonDeserialize(Gson gsonIn, String json, Class<T> adapter)
    {
        return gsonDeserialize(gsonIn, json, adapter, false);
    }

    public static <T> T gsonDeserialize(Gson gsonIn, String json, Class<T> adapter, boolean lenient)
    {
        return gsonDeserialize(gsonIn, new StringReader(json), adapter, lenient);
    }

    /**
     * Gets a human-readable description of the given JsonElement's type.  For example: "a number (4)"
     */
    public static String toString(JsonElement json)
    {
        String s = abbreviateMiddle(String.valueOf((Object)json), "...", 10);

        if (json == null)
        {
            return "null (missing)";
        }
        else if (json.isJsonNull())
        {
            return "null (json)";
        }
        else if (json.isJsonArray())
        {
            return "an array (" + s + ")";
        }
        else if (json.isJsonObject())
        {
            return "an object (" + s + ")";
        }
        else
        {
            if (json.isJsonPrimitive())
            {
                JsonPrimitive jsonprimitive = json.getAsJsonPrimitive();

                if (jsonprimitive.isNumber())
                {
                    return "a number (" + s + ")";
                }

                if (jsonprimitive.isBoolean())
                {
                    return "a boolean (" + s + ")";
                }
            }

            return s;
        }
    }

    public static String abbreviateMiddle(String str, String middle, int length) {
        if(!isEmpty(str) && !isEmpty(middle)) {
            if(length < str.length() && length >= middle.length() + 2) {
                int targetSting = length - middle.length();
                int startOffset = targetSting / 2 + targetSting % 2;
                int endOffset = str.length() - targetSting / 2;
                StringBuilder builder = new StringBuilder(length);
                builder.append(str.substring(0, startOffset));
                builder.append(middle);
                builder.append(str.substring(endOffset));
                return builder.toString();
            } else {
                return str;
            }
        } else {
            return str;
        }
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

}
