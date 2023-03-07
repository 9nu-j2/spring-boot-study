package com.kt.mytestapi.lectures;

import com.kt.mytestapi.common.ErrorsResource;
import com.kt.mytestapi.lectures.dto.LectureReqDto;
import com.kt.mytestapi.lectures.dto.LectureResDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value="api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    @PutMapping("/{id}")
    public ResponseEntity updateLecture(@PathVariable Integer id,
                                        @RequestBody @Valid LectureReqDto lectureReqDto,
                                        Errors errors){
        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
        //id와 mapping되는 entitiy가 없으면 404에러
        if(optionalLecture.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        //입력항목 체크해서 오류가 있다면 400 에러
        if(errors.hasErrors()){
            return badRequest(errors);
        }
        //입력항목 biz로직 체크해서 오류가 있다면 400 에러
        this.lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()){
            return badRequest(errors);
        }
        //id와 매핑되는 lecture entity가 있으면 Optional에서 추출
        Lecture existingLecture = optionalLecture.get();
        //LectureReqDto -> lecture 타입으로 매핑
        this.modelMapper.map(lectureReqDto, existingLecture);
        //lecture 엔티티를 db에 저장
        Lecture savedLecture = this.lectureRepository.save(existingLecture);
        //selflink오 ㅏ함께 전달하기 위해서 LectureResDto를 lectureResponse와 mapping
        LectureResDto lectureResDto = modelMapper.map(savedLecture, LectureResDto.class);
        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity getLecture(@PathVariable Integer id) {
        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
        if(optionalLecture.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Lecture lecture = optionalLecture.get();
        LectureResDto lectureResDto = modelMapper.map(lecture, LectureResDto.class);
        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping
    public ResponseEntity queryLectures(Pageable pageable, PagedResourcesAssembler<LectureResDto> assembler) {
        Page<Lecture> page = this.lectureRepository.findAll(pageable);
        Page<LectureResDto> lectureResDtoPage = page.map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        //1단계 - first, prev, next, last 링크
        //PagedModel<EntityModel<LectureResDto>> pagedResources = assembler.toModel(lectureResDtoPage);
        //2단계 - first, prev, next, last 링크 + self 링크
        //public <R extends RepresentationaModel<?>>
        //RepresentationModelAssembler의 추상메서드 R toModel(T entity)
        PagedModel<LectureResource> pagedResources =
                assembler.toModel(lectureResDtoPage, lectureResDto -> {
                    return new LectureResource(lectureResDto);
                });
        return ResponseEntity.ok(pagedResources);
    }

    @PostMapping
    public ResponseEntity createLecture(@RequestBody @Valid LectureReqDto lectureReqDto, Errors errors) {

        //입력항목 체크
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }
        //입력항목의 biz logic 체크
        lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }

        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        //free, offline 값을 갱신
        lecture.update();

        Lecture savedLecture = lectureRepository.save(lecture);
        LectureResDto lectureResDto = modelMapper.map(savedLecture, LectureResDto.class);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(LectureController.class).slash(lecture.getId());
        URI createUri = selfLinkBuilder.toUri();

        LectureResource lectureResource = new LectureResource(lectureResDto);
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }


    private static ResponseEntity<Errors> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(errors);
    }
}
