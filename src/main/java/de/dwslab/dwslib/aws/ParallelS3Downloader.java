package de.dwslab.dwslib.aws;

import java.io.File;

public class ParallelS3Downloader
	extends ParallelS3Processor
{
	/*private String s3bucket;
	private String prefix;*/
	private String localFolder;
	//private S3Helper s3;
	private boolean overwrite;

	/*public static void main(String[] args)
	{
		if(args.length!=6)
		{
			System.out.println("Usage: ParallelS3Downloader s3bucket s3prefix local-folder aws-key aws-secret overwrite[true|false]");
			return;
		}
		
		try {
			new ParallelS3Downloader(
					Runtime.getRuntime().availableProcessors(), 
					args[0], 
					args[1], 
					args[2], 
					args[3], 
					args[4], 
					Boolean.parseBoolean(args[5])).process();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	public ParallelS3Downloader(int numThreads, S3Credentials credentials, CommandTarget target, String localFolder, boolean overwriteExisting, boolean requesterPays) throws Exception {
		super(numThreads, credentials, target);
		
		/*s3bucket = sourceS3Bucket;
		prefix = sourceS3Prefix;*/
		this.localFolder = localFolder;
		overwrite = overwriteExisting;
		
		getS3().setRequestPaysEnabled(requesterPays);
		
		/*if(s3bucket==null)
			throw new Exception("Source bucket cannot be null!");*/
		if(this.localFolder==null)
			throw new Exception("Local folder cannot be null!");
		
		if(!this.localFolder.endsWith("/"))
			this.localFolder += "/";
		
		/*if(awsAccessKey==null)
			throw new Exception("AWS Access key cannot be null!");
		if(awsSecret==null)
			throw new Exception("AWS Secret key cannot be null!");*/
		
		//s3 = new S3Helper(awsAccessKey, awsSecret);
	}

	/*@Override
	protected List<String> fillListToProcess() {
		System.out.println("Loading file list from S3 ...");
		return s3.ListBucketContents(this.s3bucket, this.prefix);
	}*/

	/*@Override
	protected void process(String object) {
		File f = new File(localFolder, object);
		
		if(!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		
		if(!f.exists() || overwrite)
			s3.LoadFileFromS3(f.getAbsolutePath(), object, this.s3bucket);
	}*/

	@Override
	protected void process(S3File object) throws Exception {
		File f = new File(localFolder, object.get_key());
		
		if(!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		
		if(!f.exists() || overwrite)
			getS3().LoadFileFromS3(f.getAbsolutePath(), object.get_key(), object.get_bucket());
		
		setDone(object);
	}
	
}
