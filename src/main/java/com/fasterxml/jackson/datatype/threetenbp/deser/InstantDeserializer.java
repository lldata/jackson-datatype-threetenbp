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
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.Temporal;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Deserializer for Java 8 temporal {@link Instant}s, {@link OffsetDateTime}, and {@link ZonedDateTime}s.
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public abstract class InstantDeserializer<T extends Temporal> extends ThreetenbpDeserializerBase<T>
{
    private static final long serialVersionUID = 1L;

    abstract T parse(CharSequence str);

    abstract T fromMilliseconds(FromIntegerArguments ms);

    abstract T fromNanoseconds(FromDecimalArguments ns);

    abstract T adjust(T t, ZoneId zone);

    private InstantDeserializer(Class<T> supportedType) {
        super(supportedType);
    }

        public static final InstantDeserializer<Instant> INSTANT = new InstantDeserializer<Instant>(Instant.class) {
        Instant parse(CharSequence str) {
            return Instant.parse(str);
        }

        Instant fromMilliseconds(FromIntegerArguments ms) {
            return Instant.ofEpochMilli(ms.value);
        }

        Instant fromNanoseconds(FromDecimalArguments ns) {
            return Instant.ofEpochSecond(ns.integer, ns.fraction);
        }

        Instant adjust(Instant temporal, ZoneId zone) {
            return temporal;
        }
    };

    public static final InstantDeserializer<OffsetDateTime> OFFSET_DATE_TIME = new InstantDeserializer<OffsetDateTime>(OffsetDateTime.class) {
        @Override
        OffsetDateTime parse(CharSequence str) {
            return OffsetDateTime.parse(str);
        }

        @Override
        OffsetDateTime fromMilliseconds(FromIntegerArguments a) {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId);
        }

        @Override
        OffsetDateTime fromNanoseconds(FromDecimalArguments a) {
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId);
        }

        @Override
        OffsetDateTime adjust(OffsetDateTime d, ZoneId z) {
            return d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime()));
        }
    };

    public static final InstantDeserializer<ZonedDateTime> ZONED_DATE_TIME = new InstantDeserializer<ZonedDateTime>(ZonedDateTime.class) {
        @Override
        ZonedDateTime parse(CharSequence str) {
            return ZonedDateTime.parse(str);
        }

        @Override
        ZonedDateTime fromMilliseconds(FromIntegerArguments a) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId);
        }

        @Override
        ZonedDateTime fromNanoseconds(FromDecimalArguments a) {
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId);
        }

        @Override
        ZonedDateTime adjust(ZonedDateTime d, ZoneId z) {
            return d.withZoneSameInstant(z);
        }
    };

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
                return this.fromNanoseconds(new FromDecimalArguments(
                        seconds, nanoseconds, this.getZone(context)
                ));

            case VALUE_NUMBER_INT:
                if(context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS))
                {
                    return this.fromNanoseconds(new FromDecimalArguments(
                            parser.getLongValue(), 0, this.getZone(context)
                    ));
                }
                else
                {
                    return this.fromMilliseconds(new FromIntegerArguments(
                            parser.getLongValue(), this.getZone(context)
                    ));
                }

            case VALUE_STRING:
                String string = parser.getText().trim();
                if(string.length() == 0)
                    return null;
                if(context.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE))
                    return this.adjust(this.parse(string), this.getZone(context));
                return this.parse(string);
        }
        throw context.mappingException("Expected type float, integer, or string.");
    }

    private ZoneId getZone(DeserializationContext context)
    {
        // Instants are always in UTC, so don't waste compute cycles
        return this._valueClass == Instant.class ? null : ZoneId.of(context.getTimeZone().getID());
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
