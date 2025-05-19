
#!/usr/bin/env python3
import os
from tensorflow_lite_support.metadata_writers.bert import BertMetadataWriter
from tensorflow_lite_support.metadata_writers.writer_utils import load_file, save_file
from tensorflow_lite_support.metadata.python import metadata as _metadata

def build_bert_metadata(model_path, vocab_path, label_path, out_path):
    # 1) 모델 버퍼 로드
    model_buffer = load_file(model_path)

    # 2) BERT용 MetadataWriter 생성
    writer = BertMetadataWriter.create_for_inference(
        model_buffer=model_buffer,
        vocab_file=vocab_path,
        do_lower_case=False,  # 모델 학습 시 설정에 맞춰
        max_seq_len=128,
        label_file=label_path
    )

    # 3) 메타데이터가 포함된 버퍼 생성
    metadata_buf = writer.populate()

    # 4) .tflite 파일로 저장
    save_file(metadata_buf, out_path)

    # 5) 메타데이터 확인
    displayer = _metadata.MetadataDisplayer.with_model_file(out_path)
    print("---- Metadata JSON ----")
    print(displayer.get_metadata_json())
    print("---- Associated Files ----")
    print(displayer.get_packed_associated_file_list())

if __name__ == "__main__":
    # 프로젝트 루트 기준 경로, 필요에 따라 조정
    MODEL_IN   = "app/src/main/assets/kobert/tflite_kobert_cls_fp16/model.tflite"
    VOCAB_FILE = "vocab.txt"
    LABEL_FILE = "labels.txt"
    MODEL_OUT  = "app/src/main/assets/kobert/tflite_kobert_cls_fp16/model_with_metadata.tflite"

    build_bert_metadata(MODEL_IN, VOCAB_FILE, LABEL_FILE, MODEL_OUT)
    print(f"Metadata가 추가된 모델: {MODEL_OUT}")
