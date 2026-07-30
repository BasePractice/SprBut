package ru.sprbut.m10.extended;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.sprbut.m10.lombok.CustomerDto;
import ru.sprbut.m10.lombok.CustomerEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * <b>Расширенный пример модуля 10.</b>
 * <p>
 * MapStruct: интерфейс есть, реализации нет. {@code MapStructProcessor}
 * сгенерирует {@code CustomerMapperImpl} при компиляции — обычный класс
 * с прямыми вызовами геттеров и сеттеров, без рефлексии.
 * <p>
 * Здесь Lombok и MapStruct работают в паре, и порядок процессоров критичен:
 * MapStruct должен увидеть геттеры, которые Lombok дописал в
 * {@link CustomerEntity}, а конструктор и билдер — в {@link CustomerDto}.
 * За согласование отвечает {@code lombok-mapstruct-binding} в pom.xml.
 * <p>
 * Опция {@code -Amapstruct.unmappedTargetPolicy=ERROR} делает забытое свойство
 * <b>ошибкой сборки</b>. Это ровно то, чего не может дать рефлексивный
 * {@code BeanUtils.copyProperties}: там пропущенное поле просто останется null.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    /** Точка доступа без Spring: MapStruct умеет и просто через ServiceLoader. */
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    /** Дата, относительно которой считается возраст — чтобы тесты были детерминированными. */
    LocalDate REFERENCE_DATE = LocalDate.of(2026, 7, 30);

    @Mapping(target = "fullName", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(target = "age", source = "birthDate", qualifiedByName = "toAge")
    @Mapping(target = "status", source = "vip", qualifiedByName = "toStatus")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "id", source = "id")
    CustomerDto toDto(CustomerEntity entity);

    /** Маппинг коллекций MapStruct генерирует сам по одиночному методу. */
    List<CustomerDto> toDtos(List<CustomerEntity> entities);

    @Named("toAge")
    static int toAge(LocalDate birthDate) {
        return birthDate == null ? 0 : Period.between(birthDate, REFERENCE_DATE).getYears();
    }

    @Named("toStatus")
    static String toStatus(boolean vip) {
        return vip ? "VIP" : "STANDARD";
    }

    /**
     * {@code @AfterMapping} с {@code @MappingTarget} билдера — последний штрих
     * уже после основного маппинга. Так добавляют вычисляемые поля и нормализацию.
     */
    @AfterMapping
    static void normalizeBalance(CustomerEntity source,
                                 @MappingTarget CustomerDto.CustomerDtoBuilder target) {
        if (source.getBalance() == null) {
            target.balance(BigDecimal.ZERO);
        }
    }
}
