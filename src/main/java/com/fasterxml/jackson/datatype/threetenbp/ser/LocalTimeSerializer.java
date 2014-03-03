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

package com.fasterxml.jackson.datatype.threetenbp.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoField;

/**
 * Serializer for Java 8 temporal {@link LocalTime}s.
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public class LocalTimeSerializer extends ThreetenbpArraySerializerBase<LocalTime>
{
    public static final LocalTimeSerializer INSTANCE = new LocalTimeSerializer();

    private LocalTimeSerializer()
    {
        super(LocalTime.class);
    }

    @Override
    public void serialize(LocalTime time, JsonGenerator generator, SerializerProvider provider) throws IOException
    {
        if(provider.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
        {
            generator.writeStartArray();
            generator.writeNumber(time.getHour());
            generator.writeNumber(time.getMinute());
            if(time.getSecond() > 0 || time.getNano() > 0)
            {
                generator.writeNumber(time.getSecond());
                if(time.getNano() > 0)
                {
                    if(provider.isEnabled(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS))
                        generator.writeNumber(time.getNano());
                    else
                        generator.writeNumber(time.get(ChronoField.MILLI_OF_SECOND));
                }
            }
            generator.writeEndArray();
        }
        else
        {
            generator.writeString(time.toString());
        }
    }
}
