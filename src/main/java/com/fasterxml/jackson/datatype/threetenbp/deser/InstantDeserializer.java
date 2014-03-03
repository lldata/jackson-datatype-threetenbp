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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.threetenbp.DecimalUtils;

import java.io.IOException;
import java.math.BigDecimal;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.Temporal;

/**
 * Deserializer for Java 8 temporal {@link Instant}s, {@link OffsetDateTime}, and {@link ZonedDateTime}s.
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public abstract class InstantDeserializer<T extends Temporal> extends ThreetenbpDeserializerBase<T>
{
    private static final long serialVersionUID = 1L;

    public static final InstantDeserializer INSTANT = new InstantDeserializer(Instant.class) {
        Instant parse(CharSequence str) {
            return Instant.parse(str);
        }

        Instant fromMilliseconds(long ms) {
            return Instant.ofEpochMilli(ms);
        }

        Instant fromNanoseconds(FromDecimalArguments ns) {
            return Instant.ofEpochSecond(ns.integer, ns.fraction);
        }

        Instant adjust(Instant ts, ZoneId zone) {
            return ts;
        }
    };

    public static final InstantDeserializer<OffsetDateTime> OFFSET_DATE_TIME = new InstantDeserializer(OffsetDateTime.class);

    //    public static final InstantDeserializer<Instant> INSTANT = new InstantDeserializer<>(
    //            Instant.class, Instant::parse,
    //            a -> Instant.ofEpochMilli(a.value),
    //            a -> Instant.ofEpochSecond(a.integer, a.fraction),
    //            null
    //    );

//    public static final InstantDeserializer<OffsetDateTime> OFFSET_DATE_TIME = new InstantDeserializer<>(
//            OffsetDateTime.class, OffsetDateTime::parse,
//            a -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
//            a -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
//            (d, z) -> d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime()))
//    );

    public static final InstantDeserializer<ZonedDateTime> ZONED_DATE_TIME = new InstantDeserializer<>(
            ZonedDateTime.class, ZonedDateTime::parse,
            a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
            a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
            ZonedDateTime::withZoneSameInstant
    );

    abstract T parse(CharSequence str);

    abstract T fromMilliseconds(long ms);

    abstract T fromNanoseconds(FromDecimalArguments ns);

    abstract T adjust(T t, ZoneId zone);

    private InstantDeserializer(Class<T> supportedType) {
    {
        super(supportedType);
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        //NOTE: Timestamps contain no timezone info, and are always in configured TZ. Only
        //string values have to be adjusted to the configured TZ.
        switch(parser.getCurrentToken())
        {
            case VALUE_NUMBER_FLOAT:
                BigDecimal value = parser.getDecimalValue();
                long seconds = value.longValue();
                int nanoseconds = DecimalUtils.extractNanosecondDecimal(value, seconds);
                return this.fromNanoseconds.apply(new FromDecimalArguments(
                        seconds, nanoseconds, this.getZone(context)
                ));

            case VALUE_NUMBER_INT:
                if(context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS))
                {
                    return this.fromNanoseconds.apply(new FromDecimalArguments(
                            parser.getLongValue(), 0, this.getZone(context)
                    ));
                }
                else
                {
                    return this.fromMilliseconds.apply(new FromIntegerArguments(
                            parser.getLongValue(), this.getZone(context)
                    ));
                }

            case VALUE_STRING:
                String string = parser.getText().trim();
                if(string.length() == 0)
                    return null;
                if(context.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE))
                    return this.adjust.apply(this.parse.apply(string), this.getZone(context));
                return this.parse.apply(string);
        }
        throw context.mappingException("Expected type float, integer, or string.");
    }

    private ZoneId getZone(DeserializationContext context)
    {
        // Instants are always in UTC, so don't waste compute cycles
        return this._valueClass == Instant.class ? null : context.getTimeZone().toZoneId();
    }

    private static class FromIntegerArguments
    {
        public final long value;
        public final ZoneId zoneId;

        private FromIntegerArguments(long value, ZoneId zoneId)
        {
            this.value = value;
            this.zoneId = zoneId;
        }
    }

    private static class FromDecimalArguments
    {
        public final long integer;
        public final int fraction;
        public final ZoneId zoneId;

        private FromDecimalArguments(long integer, int fraction, ZoneId zoneId)
        {
            this.integer = integer;
            this.fraction = fraction;
            this.zoneId = zoneId;
        }
    }
}
