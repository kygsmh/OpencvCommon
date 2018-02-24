package com.opencv.common.UI.util;


public class Log {

    private static boolean gEnableLog		= true;

	private static final int SEVERITY_VERBOSE		= 4;
	private static final int SEVERITY_DEBUG			= 3;
	private static final int SEVERITY_INFORMATION	= 2;
	private static final int SEVERITY_WARNING		= 1;
	private static final int SEVERITY_ERROR			= 0;


	private static int gSeverity		= SEVERITY_VERBOSE;
	private static boolean gInitialized = false;
	
	public static void initialize()
	{
		gInitialized = true;
	}
	public static void v(String tag, String msg)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_VERBOSE)
			android.util.Log.v(tag, msg);
	}
	
	public static void i(String tag, String msg)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_INFORMATION)
			android.util.Log.i(tag, msg);
	}
	
	public static void e(String tag, String msg, Throwable e)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_ERROR)
			android.util.Log.e(tag, msg, e);
	}
	
	public static void e(String tag, String msg)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_ERROR)
			android.util.Log.e(tag, msg);
	}
	
	public static void w(String tag, String msg)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_WARNING)
			android.util.Log.w(tag, msg);
	}
	
	public static void w(String tag, String msg, Throwable e)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_WARNING)
			android.util.Log.w(tag, msg, e);
	}
	
	public static void d(String tag, String msg, Throwable e)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_DEBUG)
			android.util.Log.d(tag, msg, e);
	}
	
	public static void d(String tag, String msg)
	{
		if (!gInitialized) initialize();
		if (gEnableLog && gSeverity >= SEVERITY_DEBUG)
			android.util.Log.d(tag, msg);
	}

	public static void d2(String tag, Object...args)
	{
		if (!gInitialized) initialize();
		
		if (gEnableLog && gSeverity >= SEVERITY_DEBUG) 
		{
			StringBuilder a = new StringBuilder(100);
						
			if (null != args)
			{
				int n = args.length;
				for (int i = 0; i < n; i++)
				{				
					a.append(args[i]);
				}
			}
			
			android.util.Log.d(tag, a.toString());
		}
	}
	
	public static void w2(String tag, Object...args)
	{
		if (!gInitialized) initialize();
		
		if (gEnableLog && gSeverity >= SEVERITY_WARNING) 
		{
			StringBuilder a = new StringBuilder();
			
			if (null != args)
			{
				int n = args.length;
				for (int i = 0; i < n; i++)
				{				
					a.append(args[i]);
				}
			}
			
			android.util.Log.w(tag, a.toString());
		}
	}

}
