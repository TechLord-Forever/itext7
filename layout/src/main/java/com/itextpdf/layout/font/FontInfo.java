/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2016 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.layout.font;

import com.itextpdf.io.font.FontCacheKey;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramDescriptor;
import com.itextpdf.io.font.FontProgramDescriptorFactory;
import com.itextpdf.io.util.ArrayUtil;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.font.PdfFont;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains all font related data to create {@link FontProgram} and {@link PdfFont}.
 * {@link FontProgramDescriptor} fetches with {@link FontProgramDescriptorFactory}.
 */
public final class FontInfo {

    private static final Map<FontCacheKey, FontProgramDescriptor> fontNamesCache = new ConcurrentHashMap<>();

    private final String fontName;
    private final byte[] fontProgram;
    private final FontProgramDescriptor descriptor;
    private final int hash;
    private final String encoding;

    private FontInfo(String fontName, byte[] fontProgram, String encoding, FontProgramDescriptor descriptor) {
        this.fontName = fontName;
        this.fontProgram = fontProgram;
        this.encoding = encoding;
        this.descriptor = descriptor;
        this.hash = calculateHashCode(fontName, fontProgram, encoding);
    }

    static FontInfo create(FontProgram fontProgram, String encoding) {
        FontProgramDescriptor descriptor = FontProgramDescriptorFactory.fetchDescriptor(fontProgram);
        return new FontInfo(descriptor.getFontName(), null, encoding, descriptor);
    }

    static FontInfo create(String fontName, String encoding) {
        FontCacheKey cacheKey = FontCacheKey.create(fontName);
        FontProgramDescriptor descriptor = getFontNamesFromCache(cacheKey);
        if (descriptor == null) {
            descriptor = FontProgramDescriptorFactory.fetchDescriptor(fontName);
            putFontNamesToCache(cacheKey, descriptor);
        }
        return descriptor != null ? new FontInfo(fontName, null, encoding, descriptor) : null;
    }

    static FontInfo create(byte[] fontProgram, String encoding) {
        FontCacheKey cacheKey = FontCacheKey.create(fontProgram);
        FontProgramDescriptor descriptor = getFontNamesFromCache(cacheKey);
        if (descriptor == null) {
            descriptor = FontProgramDescriptorFactory.fetchDescriptor(fontProgram);
            putFontNamesToCache(cacheKey, descriptor);
        }
        return descriptor != null ? new FontInfo(null, fontProgram, encoding, descriptor) : null;
    }

    public PdfFont getPdfFont(FontProvider fontProvider) {
        try {
            return fontProvider.getPdfFont(this);
        } catch (IOException e) {
            throw new PdfException(PdfException.IoExceptionWhileCreatingFont, e);
        }
    }

    public FontProgramDescriptor getDescriptor() {
        return descriptor;
    }

    public String getFontName() {
        return fontName;
    }

    public byte[] getFontProgram() {
        return fontProgram;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FontInfo)) return false;

        FontInfo that = (FontInfo) o;
        return (fontName != null ? fontName.equals(that.fontName) : that.fontName == null)
                && Arrays.equals(fontProgram, that.fontProgram)
                && (encoding != null ? encoding.equals(that.encoding) : that.encoding == null);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        String name = descriptor.getFontName();
        if (name.length() > 0) {
            if (encoding != null) {
                return String.format("%s+%s", name, encoding);
            } else {
                return name;
            }
        }
        return super.toString();
    }

    private static int calculateHashCode(String fontName, byte[] bytes, String encoding) {
        int result = fontName != null ? fontName.hashCode() : 0;
        result = 31 * result + ArrayUtil.hashCode(bytes);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }

    private static FontProgramDescriptor getFontNamesFromCache(FontCacheKey key) {
        return fontNamesCache.get(key);
    }

    private static void putFontNamesToCache(FontCacheKey key, FontProgramDescriptor descriptor) {
        if (descriptor != null) {
            fontNamesCache.put(key, descriptor);
        }
    }
}
