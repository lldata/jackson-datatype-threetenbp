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

package com.fasterxml.jackson.datatype.threetenbp;

import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.DurationDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.ThreetenbpStringParsableDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.OffsetTimeDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.deser.YearDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.DurationSerializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.OffsetTimeSerializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.YearSerializer;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.MonthDay;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

/**
 * Class that registers this module with the Jackson core.<br>
 * <br>
 * <code>
 *     ObjectMapper mapper = new ObjectMapper();
 *     mapper.findAndRegisterModules();
 * </code><br>
 * <b>—OR—</b><br>
 * <code>
 *     ObjectMapper mapper = new ObjectMapper();
 *     mapper.registerModule(new ThreetenbpModule());
 * </code><br>
 * <br>
 * Most JSR-310 types are serialized as numbers (integers or decimals as appropriate) if the
 * {@link com.fasterxml.jackson.databind.SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} feature is enabled, and
 * otherwise are serialized in standard <a href="http://en.wikipedia.org/wiki/ISO_8601" target="_blank">ISO-8601</a>
 * string representation. ISO-8601 specifies formats for representing offset dates and times, zoned dates and times,
 * local dates and times, periods, durations, zones, and more. All JSR-310 types have built-in translation to and from
 * ISO-8601 formats.<br>
 * <br>
 * Granularity of timestamps is controlled through the companion features
 * {@link com.fasterxml.jackson.databind.SerializationFeature#WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS} and
 * {@link com.fasterxml.jackson.databind.DeserializationFeature#READ_DATE_TIMESTAMPS_AS_NANOSECONDS}. For serialization,
 * timestamps are written as fractional numbers (decimals), where the number is seconds and the decimal is fractional
 * seconds, if WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS is enabled (it is by default), with resolution as fine as
 * nanoseconds depending on the underlying JDK implementation. If WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS is disabled,
 * timestamps are written as a whole number of milliseconds. At deserialization time, decimal numbers are always read as
 * fractional second timestamps with up-to-nanosecond resolution, since the meaning of the decimal is unambiguous. The
 * more ambiguous integer types are read as fractional seconds without a decimal point if
 * READ_DATE_TIMESTAMPS_AS_NANOSECONDS is enabled (it is by default), and otherwise they are read as milliseconds.<br>
 * <br>
 * Some exceptions to this standard serialization/deserialization rule:<br>
 * <ul>
 *     <li>{@link Period}, which always results in an ISO-8601 format because Periods must be represented in years,
 *     months, and/or days.</li>
 *     <li>{@link Year}, which only contains a year and cannot be represented with a timestamp.</li>
 *     <li>{@link YearMonth}, which only contains a year and a month and cannot be represented with a timestamp.</li>
 *     <li>{@link MonthDay}, which only contains a month and a day and cannot be represented with a timestamp.</li>
 *     <li>{@link ZoneId} and {@link ZoneOffset}, which do not actually store dates and times but are supported with
 *     this module nonetheless.</li>
 *     <li>{@link LocalDate}, {@link LocalTime}, {@link LocalDateTime}, and {@link OffsetTime}, which cannot portably be
 *     converted to timestamps and are instead represented as arrays when WRITE_DATES_AS_TIMESTAMPS is enabled.</li>
 * </ul>
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public final class ThreetenbpModule extends SimpleModule
{
    private static final long serialVersionUID = 1L;

    public ThreetenbpModule()
    {
        super(PackageVersion.VERSION);

        // first deserializers
        addDeserializer(Duration.class, DurationDeserializer.INSTANCE);
        addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
        addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
        addDeserializer(LocalTime.class, LocalTimeDeserializer.INSTANCE);
        addDeserializer(MonthDay.class, ThreetenbpStringParsableDeserializer.MONTH_DAY);
        addDeserializer(OffsetDateTime.class, InstantDeserializer.OFFSET_DATE_TIME);
        addDeserializer(OffsetTime.class, OffsetTimeDeserializer.INSTANCE);
        addDeserializer(Period.class, ThreetenbpStringParsableDeserializer.PERIOD);
        addDeserializer(Year.class, YearDeserializer.INSTANCE);
        addDeserializer(YearMonth.class, ThreetenbpStringParsableDeserializer.YEAR_MONTH);
        addDeserializer(ZonedDateTime.class, InstantDeserializer.ZONED_DATE_TIME);
        addDeserializer(ZoneId.class, ThreetenbpStringParsableDeserializer.ZONE_ID);
        addDeserializer(ZoneOffset.class, ThreetenbpStringParsableDeserializer.ZONE_OFFSET);

        // then serializers:
        addSerializer(Duration.class, DurationSerializer.INSTANCE);
        addSerializer(Instant.class, InstantSerializer.INSTANT);
        addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);
        addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE);
        addSerializer(LocalTime.class, LocalTimeSerializer.INSTANCE);
        addSerializer(MonthDay.class, ToStringSerializer.instance);
        addSerializer(OffsetDateTime.class, InstantSerializer.OFFSET_DATE_TIME);
        addSerializer(OffsetTime.class, OffsetTimeSerializer.INSTANCE);
        addSerializer(Period.class, ToStringSerializer.instance);
        addSerializer(Year.class, YearSerializer.INSTANCE);
        addSerializer(YearMonth.class, ToStringSerializer.instance);
        addSerializer(ZonedDateTime.class, InstantSerializer.ZONED_DATE_TIME);
        addSerializer(ZoneId.class, ToStringSerializer.instance);
        addSerializer(ZoneOffset.class, ToStringSerializer.instance);
    }
}
