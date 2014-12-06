package org.onetwo.app.taskservice.actor;

import java.util.List;

import org.onetwo.app.taskservice.service.TaskCompleteListener;
import org.onetwo.app.taskservice.service.TaskListenerManager;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.plugins.task.utils.TaskResult;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class TaskResultActor extends UntypedActor {
	
	private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);


	private final TaskListenerManager taskListenerManager;
	
	public TaskResultActor(TaskListenerManager taskListenerManager) {
		super();
		this.taskListenerManager = taskListenerManager;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("TaskResultActor receive:\n");
		
		if(message instanceof TaskResult){
			TaskResult result = (TaskResult) message;
			logMsg.append("taskResult : ").append(message).append("\n");
			
			List<TaskCompleteListener> listeners = taskListenerManager.getTaskCompleteListeners(result.getTask().getTaskType());
			if(LangUtils.isNotEmpty(listeners)){
				for(TaskCompleteListener l : listeners){
					try {
						l.onComplete(result);
						logMsg.append("listener[").append(l.getClass().getName()).append("] onComplete succeed.");
						logger.info(logMsg.toString());
					} catch (Exception e) {
						logMsg.append("listener[").append(l.getClass().getName()).append("] onComplete error: ").append(e.getMessage());
						logger.info(logMsg.toString(), e);
					}
				}
			}else{
				logMsg.append("no TaskCompleteListener found for task type: " + result.getTask().getTaskType());
				logger.info(logMsg.toString());
			}
			
		}else{
			logMsg.append("unknown message : ").append(message);
			logger.info(logMsg.toString());
		}
	}

}