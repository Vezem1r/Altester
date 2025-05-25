import { useState, useRef, useCallback, memo } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-toastify';
import { QuestionService } from '@/services/QuestionService';
import AuthenticatedImage from './AuthenticatedImage';
import { QUESTION_TYPES } from './QuestionManagement';
import QuestionTypeSelector from './QuestionTypeSelector';

export const QUESTION_DIFFICULTY = {
  EASY: 'EASY',
  MEDIUM: 'MEDIUM',
  HARD: 'HARD',
};

const DIFFICULTY_TRANSLATION_KEYS = {
  EASY: 'questionForm.difficulty.easy',
  MEDIUM: 'questionForm.difficulty.medium',
  HARD: 'questionForm.difficulty.hard',
};

const QuestionForm = ({ testId, question, onCancel, onSuccess, isEditing }) => {
  const { t } = useTranslation();
  const [questionData, setQuestionData] = useState(
    question || {
      questionText: '',
      questionType: QUESTION_TYPES.TEXT_ONLY,
      difficulty: QUESTION_DIFFICULTY.MEDIUM,
      options: [],
      correctAnswer: '',
    }
  );

  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef(null);

  const handleChange = useCallback(e => {
    const { name, value } = e.target;
    setQuestionData(prevData => ({
      ...prevData,
      [name]: value,
    }));
  }, []);

  const handleTypeChange = useCallback(value => {
    setQuestionData(prevData => {
      let newOptions = prevData.options;
      if (
        (value === QUESTION_TYPES.MULTIPLE_CHOICE ||
          value === QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE) &&
        (!newOptions || newOptions.length === 0)
      ) {
        newOptions = [
          { text: '', correct: true },
          { text: '', correct: false },
        ];
      }

      return {
        ...prevData,
        questionType: value,
        options: newOptions,
      };
    });
  }, []);

  const handleDifficultyChange = useCallback(value => {
    setQuestionData(prevData => ({
      ...prevData,
      difficulty: value,
    }));
  }, []);

  const handleOptionChange = useCallback(
    (index, field, value) => {
      setQuestionData(prevData => {
        const updatedOptions = [...prevData.options];

        if (field === 'correct') {
          if (
            value === false &&
            updatedOptions[index].correct &&
            updatedOptions.filter(o => o.correct).length === 1
          ) {
            toast.warning(
              t(
                'questionForm.oneCorrectOption',
                'At least one option must be correct'
              )
            );
            return prevData;
          }
        }

        updatedOptions[index] = {
          ...updatedOptions[index],
          [field]: value,
        };

        return {
          ...prevData,
          options: updatedOptions,
        };
      });
    },
    [t]
  );

  const addOption = useCallback(() => {
    setQuestionData(prevData => ({
      ...prevData,
      options: [...prevData.options, { text: '', correct: false }],
    }));
  }, []);

  const removeOption = useCallback(index => {
    setQuestionData(prevData => {
      const newOptions = prevData.options.filter((_, i) => i !== index);

      if (
        prevData.options[index].correct &&
        !newOptions.some(option => option.correct) &&
        newOptions.length > 0
      ) {
        newOptions[0].correct = true;
      }

      return {
        ...prevData,
        options: newOptions,
      };
    });
  }, []);

  const handleImageChange = useCallback(
    e => {
      const file = e.target.files[0];
      if (!file) return;

      const validTypes = [
        'image/jpeg',
        'image/png',
        'image/gif',
        'image/svg+xml',
      ];
      if (!validTypes.includes(file.type)) {
        toast.error(
          t(
            'questionForm.invalidImageType',
            'Please select a valid image file (JPG, PNG, GIF, SVG)'
          )
        );
        e.target.value = null;
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        toast.error(
          t('questionForm.imageTooLarge', 'Image file is too large (max 5MB)')
        );
        e.target.value = null;
        return;
      }

      setImageFile(file);

      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    },
    [t]
  );

  const clearImage = useCallback(() => {
    setImageFile(null);
    setImagePreview(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }

    if (isEditing && questionData.imagePath) {
      setQuestionData(prevData => ({
        ...prevData,
        removeImage: true,
      }));
    }
  }, [isEditing, questionData.imagePath]);

  const validateQuestionData = useCallback(() => {
    if (!questionData.difficulty) {
      throw new Error(
        t('questionForm.selectDifficulty', 'Please select a difficulty level')
      );
    }

    if (
      questionData.correctAnswer &&
      questionData.correctAnswer.length > 1000
    ) {
      throw new Error(
        t(
          'questionForm.correctAnswerTooLong',
          'Correct answer must be less than 1000 characters'
        )
      );
    }

    switch (questionData.questionType) {
      case QUESTION_TYPES.TEXT_ONLY:
        if (
          !questionData.questionText ||
          questionData.questionText.trim() === ''
        ) {
          throw new Error(
            t('questionForm.enterQuestionText', 'Please enter question text')
          );
        }
        break;

      case QUESTION_TYPES.IMAGE_ONLY:
        if (
          (!imageFile && !questionData.imagePath) ||
          (isEditing && questionData.removeImage && !imageFile)
        ) {
          throw new Error(
            t(
              'questionForm.uploadImagePlease',
              'Please upload an image for this question'
            )
          );
        }
        if (
          questionData.questionText &&
          questionData.questionText.trim() !== ''
        ) {
          throw new Error(
            t(
              'questionForm.noTextForImageOnly',
              'Text should be empty for image-only questions'
            )
          );
        }
        break;

      case QUESTION_TYPES.TEXT_WITH_IMAGE:
        if (
          !questionData.questionText ||
          questionData.questionText.trim() === ''
        ) {
          throw new Error(
            t('questionForm.enterQuestionText', 'Please enter question text')
          );
        }
        if (
          (!imageFile && !questionData.imagePath) ||
          (isEditing && questionData.removeImage && !imageFile)
        ) {
          throw new Error(
            t(
              'questionForm.uploadImagePlease',
              'Please upload an image for this question'
            )
          );
        }
        break;

      case QUESTION_TYPES.MULTIPLE_CHOICE: {
        if (
          !questionData.questionText ||
          questionData.questionText.trim() === ''
        ) {
          throw new Error(
            t('questionForm.enterQuestionText', 'Please enter question text')
          );
        }

        if (!questionData.options || questionData.options.length < 2) {
          throw new Error(
            t(
              'questionForm.twoOptionsRequired',
              'Multiple choice questions must have at least 2 options'
            )
          );
        }

        const hasEmptyOption = questionData.options.some(
          opt => !opt.text || opt.text.trim() === ''
        );
        if (hasEmptyOption) {
          throw new Error(
            t(
              'questionForm.allOptionsMustHaveText',
              'All options must have text'
            )
          );
        }

        const hasCorrectOption = questionData.options.some(
          opt => opt.correct === true
        );
        if (!hasCorrectOption) {
          throw new Error(
            t(
              'questionForm.oneOptionMustBeCorrect',
              'At least one option must be marked as correct'
            )
          );
        }
        break;
      }

      case QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE: {
        if (
          (!imageFile && !questionData.imagePath) ||
          (isEditing && questionData.removeImage && !imageFile)
        ) {
          throw new Error(
            t(
              'questionForm.uploadImagePlease',
              'Please upload an image for this question'
            )
          );
        }

        if (!questionData.options || questionData.options.length < 2) {
          throw new Error(
            t(
              'questionForm.twoOptionsRequiredForImage',
              'Image with multiple choice questions must have at least 2 options'
            )
          );
        }

        const hasEmptyImgOption = questionData.options.some(
          opt => !opt.text || opt.text.trim() === ''
        );
        if (hasEmptyImgOption) {
          throw new Error(
            t(
              'questionForm.allOptionsMustHaveText',
              'All options must have text'
            )
          );
        }

        const hasCorrectImgOption = questionData.options.some(
          opt => opt.correct === true
        );
        if (!hasCorrectImgOption) {
          throw new Error(
            t(
              'questionForm.oneOptionMustBeCorrect',
              'At least one option must be marked as correct'
            )
          );
        }

        if (
          !questionData.questionText ||
          questionData.questionText.trim() === ''
        ) {
          questionData.questionText = t(
            'questionForm.selectCorrectAnswer',
            'Select the correct answer'
          );
        }
        break;
      }

      default:
        throw new Error(
          t('questionForm.invalidQuestionType', 'Invalid question type')
        );
    }

    return { ...questionData };
  }, [questionData, imageFile, isEditing, t]);

  const handleSubmit = useCallback(
    async e => {
      e.preventDefault();

      try {
        const validatedData = validateQuestionData();
        setLoading(true);

        if (isEditing) {
          await QuestionService.updateQuestion(
            validatedData.id,
            validatedData,
            imageFile
          );
          toast.success(
            t('questionForm.questionUpdated', 'Question updated successfully')
          );
        } else {
          await QuestionService.addQuestion(testId, validatedData, imageFile);
          toast.success(
            t('questionForm.questionAdded', 'Question added successfully')
          );
        }

        if (onSuccess) {
          onSuccess();
        }
      } catch (error) {
        toast.error(
          error.message ||
            t('questionForm.errorSavingQuestion', 'Error saving question')
        );
      } finally {
        setLoading(false);
      }
    },
    [
      questionData,
      imageFile,
      isEditing,
      testId,
      onSuccess,
      validateQuestionData,
      t,
    ]
  );

  const getDifficultyTranslationKey = useCallback(difficulty => {
    return (
      DIFFICULTY_TRANSLATION_KEYS[difficulty] ||
      'questionForm.difficulty.unknown'
    );
  }, []);

  const getDifficultyColorClass = useCallback(difficulty => {
    switch (difficulty) {
      case QUESTION_DIFFICULTY.EASY:
        return 'bg-green-500';
      case QUESTION_DIFFICULTY.MEDIUM:
        return 'bg-yellow-500';
      case QUESTION_DIFFICULTY.HARD:
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  }, []);

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-600 bg-opacity-75 flex items-center justify-center">
      <div className="bg-white rounded-xl shadow-xl max-w-3xl w-full m-4 max-h-[90vh] overflow-y-auto">
        <div className="px-6 py-4 border-b border-gray-200 bg-purple-600 rounded-t-xl">
          <h3 className="text-xl leading-6 font-semibold text-white">
            {isEditing
              ? t('questionForm.editQuestion', 'Edit Question')
              : t('questionForm.addNewQuestion', 'Add New Question')}
          </h3>
          <p className="mt-1 text-sm text-purple-100">
            {isEditing
              ? t(
                  'questionForm.updateQuestionDetails',
                  'Update question details'
                )
              : t(
                  'questionForm.createNewQuestion',
                  'Create a new question for this test'
                )}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Question Type Selector */}
            <QuestionTypeSelector
              selectedType={questionData.questionType}
              onTypeChange={handleTypeChange}
            />

            <div className="md:col-span-2">
              {questionData.questionType !== QUESTION_TYPES.IMAGE_ONLY && (
                <div>
                  <label
                    htmlFor="questionText"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    {t('questionForm.questionText', 'Question Text')}
                    {questionData.questionType ===
                      QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE && (
                      <span className="ml-1 text-gray-400 text-xs">
                        ({t('questionForm.optional', 'optional')})
                      </span>
                    )}
                  </label>
                  <textarea
                    id="questionText"
                    name="questionText"
                    rows="3"
                    value={questionData.questionText || ''}
                    onChange={handleChange}
                    className="w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500"
                    placeholder={
                      questionData.questionType ===
                      QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE
                        ? t(
                            'questionForm.optionalQuestionTextPlaceholder',
                            'Enter optional question text here...'
                          )
                        : t(
                            'questionForm.questionTextPlaceholder',
                            'Enter your question text here...'
                          )
                    }
                  />
                </div>
              )}
            </div>

            {/* Difficulty */}
            <div className="md:col-span-3">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                {t('questionForm.difficultyLevel', 'Difficulty Level')}{' '}
                <span className="text-red-500">*</span>
              </label>
              <div className="flex flex-wrap gap-2">
                {Object.values(QUESTION_DIFFICULTY).map(difficulty => (
                  <button
                    key={difficulty}
                    type="button"
                    onClick={() => handleDifficultyChange(difficulty)}
                    className={`px-3 py-2 text-sm font-medium rounded-md flex items-center transition-colors duration-200 ${
                      questionData.difficulty === difficulty
                        ? 'bg-purple-100 text-purple-800 border border-purple-300'
                        : 'bg-gray-50 text-gray-800 border border-gray-200 hover:bg-gray-200'
                    }`}
                  >
                    <span
                      className={`h-2 w-2 rounded-full mr-2 ${getDifficultyColorClass(difficulty)}`}
                    />
                    {t(getDifficultyTranslationKey(difficulty), {
                      defaultValue: difficulty,
                    })}
                  </button>
                ))}
              </div>
              <p className="mt-2 text-xs text-gray-500">
                {t(
                  'questionForm.scoreAssignedAutomatically',
                  'The score is automatically assigned based on difficulty level in test settings.'
                )}
              </p>
            </div>
          </div>

          {/* Correct Answer Field */}
          <div className="border rounded-lg p-4 bg-gray-50">
            <div className="flex justify-between items-start mb-2">
              <label
                htmlFor="correctAnswer"
                className="block text-sm font-medium text-gray-700"
              >
                {t('questionForm.correctAnswer', 'Correct Answer')}
                <span className="text-gray-400 text-xs">
                  ({t('questionForm.optional', 'optional')})
                </span>
              </label>
              <span className="text-xs text-gray-500">
                {questionData.correctAnswer
                  ? questionData.correctAnswer.length
                  : 0}
                /1000
              </span>
            </div>
            <textarea
              id="correctAnswer"
              name="correctAnswer"
              rows="3"
              value={questionData.correctAnswer || ''}
              onChange={handleChange}
              maxLength={1000}
              className="w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500"
              placeholder={t(
                'questionForm.correctAnswerPlaceholder',
                'Enter the correct answer (recommended if the test will be checked by AI)'
              )}
            />
            <p className="mt-2 text-xs text-gray-500">
              {t(
                'questionForm.correctAnswerRecommendation',
                "It's recommended to provide a correct answer if the test will be checked by AI."
              )}
            </p>
          </div>

          {/* Image Upload */}
          {(questionData.questionType === QUESTION_TYPES.IMAGE_ONLY ||
            questionData.questionType === QUESTION_TYPES.TEXT_WITH_IMAGE ||
            questionData.questionType ===
              QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE) && (
            <div className="border rounded-lg p-4 bg-gray-50">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('questionForm.questionImage', 'Question Image')}
              </label>
              <div className="flex flex-col md:flex-row items-start gap-4">
                <div className="flex-grow">
                  <div className="flex items-center">
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageChange}
                      ref={fileInputRef}
                      className="sr-only"
                      id="question-image"
                    />

                    <label
                      htmlFor="question-image"
                      className="relative flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none cursor-pointer transition-colors duration-200"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-5 w-5 mr-2 text-gray-400"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                        />
                      </svg>
                      {imageFile ||
                      (isEditing &&
                        questionData.imagePath &&
                        !questionData.removeImage)
                        ? t('questionForm.changeImage', 'Change Image')
                        : t('questionForm.uploadImage', 'Upload Image')}
                    </label>

                    {(imageFile ||
                      (isEditing &&
                        questionData.imagePath &&
                        !questionData.removeImage)) && (
                      <button
                        type="button"
                        onClick={clearImage}
                        className="ml-2 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200 focus:outline-none transition-colors duration-200"
                      >
                        {t('questionForm.remove', 'Remove')}
                      </button>
                    )}
                  </div>

                  <p className="mt-2 text-xs text-gray-500">
                    {t(
                      'questionForm.imageFormatInfo',
                      'Upload JPG, PNG, GIF, or SVG image (max 5MB)'
                    )}
                  </p>
                </div>

                {/* Simplified image preview */}
                <div className="w-full md:w-48 h-48 bg-white border border-gray-200 rounded-md overflow-hidden flex items-center justify-center">
                  {imagePreview ||
                  (isEditing &&
                    questionData.imagePath &&
                    !questionData.removeImage) ? (
                    imagePreview ? (
                      <img
                        src={imagePreview}
                        alt={t('questionForm.preview', 'Preview')}
                        className="max-h-full max-w-full object-contain"
                      />
                    ) : (
                      <AuthenticatedImage
                        imagePath={questionData.imagePath}
                        alt={t('questionForm.preview', 'Preview')}
                        className="max-h-full max-w-full object-contain"
                      />
                    )
                  ) : (
                    <div className="text-center p-4 text-gray-400">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-10 w-10 mx-auto mb-2"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                        />
                      </svg>
                      <span className="text-xs">
                        {t('questionForm.noImageSelected', 'No image selected')}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Multiple Choice Options */}
          {(questionData.questionType === QUESTION_TYPES.MULTIPLE_CHOICE ||
            questionData.questionType ===
              QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE) && (
            <div className="border rounded-lg p-4 bg-gray-50">
              <div className="flex items-center justify-between mb-2">
                <label className="block text-sm font-medium text-gray-700">
                  {t(
                    'questionForm.optionsCheckCorrect',
                    'Options (Check all correct answers)'
                  )}
                </label>
                <button
                  type="button"
                  onClick={addOption}
                  className="inline-flex items-center px-2.5 py-1.5 border border-transparent text-xs font-medium rounded text-purple-700 bg-purple-100 hover:bg-purple-200 focus:outline-none transition-colors duration-200"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4 mr-1"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 4v16m8-8H4"
                    />
                  </svg>
                  {t('questionForm.addOption', 'Add Option')}
                </button>
              </div>

              <p className="text-xs text-gray-500 mb-3">
                {t(
                  'questionForm.multipleCorrectAnswersInfo',
                  'You can select multiple correct answers by checking multiple options.'
                )}
              </p>

              <div className="space-y-3 mt-3">
                {questionData.options &&
                  questionData.options.map((option, index) => (
                    <div
                      key={index}
                      className="flex items-start bg-white p-3 rounded-md border border-gray-200"
                    >
                      <div className="flex items-center h-5 mt-1">
                        <input
                          id={`correct-${index}`}
                          name={`correct-${index}`}
                          type="checkbox"
                          checked={option.correct}
                          onChange={e =>
                            handleOptionChange(
                              index,
                              'correct',
                              e.target.checked
                            )
                          }
                          className="h-4 w-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
                        />
                      </div>
                      <div className="ml-3 flex-grow">
                        <input
                          type="text"
                          value={option.text || ''}
                          onChange={e =>
                            handleOptionChange(index, 'text', e.target.value)
                          }
                          placeholder={t(
                            'questionForm.optionText',
                            'Option text'
                          )}
                          className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500 sm:text-sm"
                        />
                        <input
                          type="text"
                          value={option.description || ''}
                          onChange={e =>
                            handleOptionChange(
                              index,
                              'description',
                              e.target.value
                            )
                          }
                          placeholder={t(
                            'questionForm.optionalDescription',
                            'Optional description'
                          )}
                          className="mt-2 block w-full border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500 sm:text-xs text-gray-500"
                        />
                      </div>

                      {questionData.options.length > 2 && (
                        <button
                          type="button"
                          onClick={() => removeOption(index)}
                          className="ml-2 text-red-500 hover:text-red-700 focus:outline-none transition-colors duration-200"
                          aria-label={t(
                            'questionForm.removeOption',
                            'Remove option'
                          )}
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                            />
                          </svg>
                        </button>
                      )}
                    </div>
                  ))}
              </div>
            </div>
          )}

          {/* Form actions */}
          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none transition-colors duration-200"
            >
              {t('questionForm.cancel', 'Cancel')}
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-purple-600 hover:bg-purple-700 focus:outline-none disabled:opacity-50 transition-colors duration-200"
            >
              {loading ? (
                <>
                  <svg
                    className="animate-spin inline-block mr-2 h-4 w-4 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                  </svg>
                  {t('questionForm.saving', 'Saving...')}
                </>
              ) : isEditing ? (
                t('questionForm.updateQuestion', 'Update Question')
              ) : (
                t('questionForm.saveQuestion', 'Save Question')
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default memo(QuestionForm);
