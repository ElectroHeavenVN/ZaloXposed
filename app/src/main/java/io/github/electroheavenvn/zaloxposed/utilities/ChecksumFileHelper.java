package io.github.electroheavenvn.zaloxposed.utilities;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

public final class ChecksumFileHelper
{
    private ChecksumFileHelper() { }

    public record Result(File file, boolean duplicate) { }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Result CopyWithChecksumChecks(ContentResolver resolver, Uri uri, File dir, String extension)
    {
        File temp = new File(dir, ".tmp_" + UUID.randomUUID().toString().replace("-", ""));
        try
        {
            if (!copyStream(resolver.openInputStream(uri), temp))
                return null;
            String checksum = CalcChecksum(temp);
            if (checksum == null)
                return null;
            File[] existing = dir.listFiles((d, name) -> name.startsWith(checksum + "."));
            if (existing != null && existing.length > 0)
                return new Result(existing[0], true);
            File target = new File(dir, checksum + "." + extension);
            if (!temp.renameTo(target))
            {
                if (!copyStream(new FileInputStream(temp), target))
                    return null;
            }
            return new Result(target, false);
        }
        catch (Exception e)
        {
            Logger.e(e);
            return null;
        }
        finally
        {
            try
			{ 
				temp.delete(); 
			}
            catch (Exception ignored) { }
        }
    }

    public static String CalcChecksum(File file)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new FileInputStream(file))
            {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0)
                    digest.update(buffer, 0, len);
            }
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest.digest())
                sb.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            return sb.toString();
        }
        catch (Exception e) { Logger.e(e); }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean copyStream(InputStream source, File target)
    {
        try (InputStream in = source; OutputStream out = new FileOutputStream(target))
        {
            if (in == null)
                return false;
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0)
                out.write(buffer, 0, len);
            out.flush();
            return target.length() > 0;
        }
        catch (Exception e)
        {
            Logger.e(e);
            try 
			{
				target.delete(); 
			}
            catch (Exception ignored) { }
        }
        return false;
    }
}