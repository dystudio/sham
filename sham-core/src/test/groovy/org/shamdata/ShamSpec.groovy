package org.shamdata

import spock.lang.Specification

import spock.lang.Unroll

import org.shamdata.test.TestUtil
import org.shamdata.person.PersonGenerator
import org.shamdata.text.MarkovGenerator
import org.shamdata.text.SpewGenerator
import org.shamdata.image.ImagePicker
import org.shamdata.text.MarkovGeneratorSpec

class ShamSpec extends Specification {

	def cleanup() {
		System.properties.remove(Sham.SHAM_SEED_SYSTEM_PROPERTY_KEY)
	}

    def "getInstance creates new instance if none set"() {
        given: 'null instance'
            Sham.setInstance(null)

        when: 'call get instance'
            def sham = Sham.getInstance()

        then: 'get a sham instance back'
            sham != null

        expect: 'call getinstace again, get same instance back'
            Sham.getInstance() == sham
    }

    def "nextPerson returns new person"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for next person'
            def person = sham.nextPerson()

        then: 'get populated person'
            person
            person.firstName
            person.lastName
            person.gender
            person.name
    }

    def "nextImage returns new image"() {
        given: 'new sham instance'
            def sham = new Sham()
            sham.imageBaseDir = "${TestUtil.projectBaseDir}/src/test"

        when: 'ask for next image'
            def image = sham.nextImage("images")

        then: 'get valid image url'
            def validImages = new File("${TestUtil.projectBaseDir}/src/test/images").listFiles({ dir, name -> !name.startsWith('.') } as FilenameFilter).findAll{it.isFile()}*.toURL()
            image in validImages

    }

    def "nextImageSet returns new image set"() {
        given: 'new sham instance'
            def sham = new Sham()
            sham.imageBaseDir = "${TestUtil.projectBaseDir}/src/test"

        when: 'ask for next image set'
            def imageSet = sham.nextImageSet("images")

        then: 'get valid image url'
            def validOtherImage0s = new File("${TestUtil.projectBaseDir}/src/test/images").listFiles().findAll{it.isDirectory() && !it.name.startsWith('.')}.collect{new File(it, "otherImage0.jpg")}*.toURL()
            imageSet.otherImage0 in validOtherImage0s
    }

    def "nextHeadline returns new headline"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for a sentence'
            def sentence = sham.nextHeadline()

        then: 'get something looking like a sentence '
            sentence
            sentence.length() > 0
            sentence =~ /.*[!?.].*/
    }

    def "nextProductName returns new product"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for a product name'
            def name = sham.nextProductName()

        then: 'get something looking like a name '
            name
            name.length() > 0
    }

    def "nextSentence returns new sentence"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for a sentence'
            def sentence = sham.nextSentence()

        then: 'get something looking like a sentence'
            sentence
            sentence.length() > 0
            sentence.substring(sentence.length() - 1) in MarkovGeneratorSpec.END_OF_SENTENCE_CHARS
    }

    def "nextSentence with max chars returns new sentence"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for a sentence'
            def sentence = sham.nextSentence(30)

        then: 'get something looking like a sentence'
            sentence
            sentence.length() > 0
            sentence.length() <= 30
            sentence.substring(sentence.length() - 1) in MarkovGeneratorSpec.END_OF_SENTENCE_CHARS
    }

    def "nextParagraph returns new paragraph"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for a paragraph'
            def para = sham.nextParagraph()

        then: 'get something looking like a paragraph'
            looksLikeAParagraph(para)
    }

    private boolean looksLikeAParagraph(String para) {
        para.split("[!.?]").size() >= 2
    }

    def "nextParagraphs returns some number of paragraphs"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for a paragraph'
            def paras = sham.nextParagraphs()

        then: 'some expected number of paragraphs'
            paras.size() >= 1
            paras.size() <= 8

        and: 'each item looks like a paragraph'
            paras.each {
                assert looksLikeAParagraph(it)
            }
    }

    @Unroll({"nextParagraphs with $num returns expected number of paragraphs"})
    def "nextParagraphs with parameter returns expected number of paragraphs"() {
        given: 'new sham instance'
            def sham = new Sham()

        when: 'ask for a paragraph'
            def paras = sham.nextParagraphs(num)

        then: 'some expected number of paragraphs'
            paras.size() == num

        and: 'each item looks like a paragraph'
            paras.each {
                assert looksLikeAParagraph(it)
            }
        where:
            num << [1, 3, 5]
    }
	
	def "set seed sets seed to given state"() {
		given: 'sham instance'
			def sham = new Sham()
			long newSeed = 1234L

		when: 'set seed'
			sham.seed = newSeed

		then: 'set on underlying random instance'
			sham.random.seed.get() == newSeed
	}

	def "get seed returns seed to given state"() {
		given: 'sham instance'
			def sham = new Sham()

		expect: 'get seed return underlying random seed'
			sham.seed == sham.random.seed.get()
	}

	def "sham loads seed from system property if found"() {
		given: 'system property'
			def seed = 1234L
			System.setProperty(Sham.SHAM_SEED_SYSTEM_PROPERTY_KEY, seed as String)
		
		when: 'create new instance'
			def sham = new Sham()
		
		then: 'underlying seed is as specified'
			sham.seed == seed
	}

    def "sham has default set of generators"() {
        given: 'sham instance'
            def sham = new Sham(imageBaseDir: '.');

        when: 'ask for various types of content'
            sham.nextPerson()
            sham.nextSentence()
            sham.nextHeadline()
            sham.nextProductName()
            sham.nextImage(".")

        and: 'ask for generators'
            def generators = sham.generators

        then: 'get expected set of generators'
            generators.size() == 5

            generators.person instanceof PersonGenerator
            generators.text instanceof MarkovGenerator
            generators.headline instanceof SpewGenerator
            generators.productName instanceof SpewGenerator
            generators["image/."] instanceof ImagePicker
    }

    def "generator registered with sham has sham's random number generator set and appear in list"() {
        given: 'sham instance'
            def sham = new Sham();

        and: 'new generator'
            def gen = Mock(ShamGenerator)

        when: 'register generator'
            sham.registerGenerator("newGenerator", gen)

        then: 'random number set on generator'
            1 * gen.setRandom(sham.random)

        and: 'generator is returned when asked for'
            gen == sham.getGenerator("newGenerator")

        and: 'generator is included in list of generators'
            sham.generators.newGenerator == gen

    }
}
