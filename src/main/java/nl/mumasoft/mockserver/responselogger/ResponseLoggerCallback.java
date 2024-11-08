package nl.mumasoft.mockserver.responselogger;

import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ResponseLoggerCallback implements ExpectationResponseCallback
{
	private final String outputPath;
	private final DateTimeFormatter dtf;
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseLoggerCallback.class);

	public ResponseLoggerCallback()
	{
		String output = System.getenv("REQUEST_OUTPUT_PATH");
		if (output == null)
			output = "/tmp";

		this.outputPath = output;
		this.dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.S");
	}

	@Override
	public HttpResponse handle(HttpRequest httpRequest) throws Exception
	{
		String requestBody = httpRequest.getBodyAsString();
		if (requestBody != null)
		{
			boolean isProd = httpRequest.getPath().toString().contains("prod");
			String prefix = isProd ? "prod" : "test";
			Path output = Path.of(outputPath, prefix + "-" + LocalDateTime.now().format(dtf));
			LOGGER.info("Received request and writing output to {}", output);
			Files.write(output, httpRequest.getBodyAsRawBytes(), CREATE_NEW);
		}

		return (new HttpResponse()).withStatusCode(200);
	}
}
