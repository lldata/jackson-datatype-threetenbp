/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package com.fasterxml.jackson.datatype.threetenbp.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.threetenbp.DecimalUtils;

import java.io.IOException;
import java.math.BigDecimal;
import org.threeten.bp.Duration;

/**
 * Deserializer for Java 8 temporal {@link Duration}s.
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public class DurationDeserializer extends ThreetenbpDeserializerBase<Duration>
{
    private static final long serialVersionUID = 1L;

    public static final DurationDeserializer INSTANCE = new DurationDeserializer();

    private DurationDeserializer()
    {
        super(Duration.class);
    }

    @Override
    public Duration deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        switch(parser.getCurrentToken())
        {
            case VALUE_NUMBER_FLOAT:
                BigDecimal value = parser.getDecimalValue();
                long seconds = value.longValue();
                int nanoseconds = DecimalUtils.extractNanosecondDecimal(value, seconds);
                return Duration.ofSeconds(seconds, nanoseconds);

            case VALUE_NUMBER_INT:
                if(context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS))
                    return Duration.ofSeconds(parser.getLongValue());
                else
                    return Duration.ofMillis(parser.getLongValue());

            case VALUE_STRING:
                String string = parser.getText().trim();
                if(string.length() == 0)
                    return null;
                return Duration.parse(string);
        }

        throw context.mappingException("Expected type float, integer, or string.");
    }
}
