package com.enliple.outviserbatch.common.exception;

/**
 * 다시구현필요
 * @author enliple
 *
 */
//@RestControllerAdvice
public class CommonExceptionAdvice {
//    
//    Date now = new Date();
//    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    String timestampStr = df.format(now);
//    
//    /**
//     *  "status": 500,
//     *  "timestamp": "2019-01-17T16:12:45.977+0000",
//     *  "code": 500,
//     *  "error": "Internal Server Error",
//     *  "message": "Error processing the request!",
//     *  "path": "/my-endpoint-with-exceptions"
//     * @param request
//     * @param e
//     * @return
//     */
//    @ExceptionHandler({CommonException.class})
//    public ResponseEntity<CommonExceptionEntity> exceptionHandler(HttpServletRequest request, final CommonException ce) {
//        return ResponseEntity
//                .status(ce.getCommonExceptionEnum().getCeHttpStatusEnum())
//                .body(CommonExceptionEntity.builder()
//                        .bodyTimestamp(timestampStr)
//                        .bodyCode(ce.getCommonExceptionEnum().getCeCodeEnum())
//                        .bodyError(ce.getCommonExceptionEnum().getCeErrorEnum())
//                        .bodyMessage(ce.getRsMessage())
//                        .build());
//    }
//    
//    @ExceptionHandler({RuntimeException.class})
//    public ResponseEntity<CommonExceptionEntity> exceptionHandler(HttpServletRequest request, final RuntimeException e) {
//        return ResponseEntity
//                .status(CommonExceptionEnum.BAD_REQUEST.getCeHttpStatusEnum())
//                .body(CommonExceptionEntity.builder()
//                        .bodyTimestamp(timestampStr)
//                        .bodyCode(CommonExceptionEnum.BAD_REQUEST.getCeCodeEnum())
//                        .bodyError(e.toString())
//                        .bodyMessage(CommonExceptionEnum.BAD_REQUEST.getCeErrorEnum())
//                        .build());
//    }
//    
//    @ExceptionHandler({Exception.class})
//    public ResponseEntity<CommonExceptionEntity> exceptionHandler(HttpServletRequest request, final Exception e) {
//        return ResponseEntity
//                .status(CommonExceptionEnum.INTERNAL_SERVER_ERROR.getCeHttpStatusEnum())
//                .body(CommonExceptionEntity.builder()
//                        .bodyTimestamp(timestampStr)
//                        .bodyCode(CommonExceptionEnum.INTERNAL_SERVER_ERROR.getCeCodeEnum())
//                        .bodyError(e.toString())
//                        .bodyMessage(CommonExceptionEnum.INTERNAL_SERVER_ERROR.getCeErrorEnum())
//                        .build());
//    }
}