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
