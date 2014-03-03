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
import org.threeten.bp.*;

import java.io.IOException;

/**
 * Deserializer for all Java 8 temporal {@link org.threeten.bp} types that cannot be represented with numbers and that have
 * parse functions that can take {@link String}s.
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public abstract class ThreetenbpStringParsableDeserializer<T> extends ThreetenbpDeserializerBase<T>
{
    private static final long serialVersionUID = 1L;

    public static final ThreetenbpStringParsableDeserializer<MonthDay> MONTH_DAY =
            new ThreetenbpStringParsableDeserializer(MonthDay.class) {
                public MonthDay parse(String s) {
                    return MonthDay.parse(s);
                }
            };

    public static final ThreetenbpStringParsableDeserializer<Period> PERIOD =
            new ThreetenbpStringParsableDeserializer(Period.class) {
                public Period parse(String s) {
                    return Period.parse(s);
                }
            };

    public static final ThreetenbpStringParsableDeserializer<YearMonth> YEAR_MONTH =
            new ThreetenbpStringParsableDeserializer(YearMonth.class) {
                public YearMonth parse(String s) {
                    return YearMonth.parse(s);
                }
            };

    public static final ThreetenbpStringParsableDeserializer<ZoneId> ZONE_ID =
            new ThreetenbpStringParsableDeserializer(ZoneId.class) {
                public ZoneId parse(String s) {
                    return ZoneId.of(s);
                }
            };

    public static final ThreetenbpStringParsableDeserializer<ZoneOffset> ZONE_OFFSET =
            new ThreetenbpStringParsableDeserializer(ZoneOffset.class) {
                public ZoneOffset parse(String s) {
                    return ZoneOffset.of(s);
                }
            };

    private ThreetenbpStringParsableDeserializer(Class<T> supportedType)
    {
        super(supportedType);
    }

    public abstract T parse(String s);

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        String string = parser.getText().trim();
        if(string.length() == 0)
            return null;
        return parse(string);
    }
}
